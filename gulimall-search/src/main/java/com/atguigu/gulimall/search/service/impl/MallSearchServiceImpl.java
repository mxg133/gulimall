package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.AttrResponseVo;
import com.atguigu.gulimall.search.vo.BrandVo;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 孟享广
 * @date 2021-01-14 10:04 上午
 * @description
 */

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    /**
     * 去es进行检索
     * @param param 检索的所有参数
     * @return 返回检索的结果
     */
    @Override
    public SearchResult search(SearchParam param) {
        //1 动态构建出查询需要的DSL语句
        SearchResult result = null;

        //1 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            //2 执行检索请求
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            //3 分析响应数据，封装成我们需要的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 准备检索请求 查询！
     * 模糊匹配 过滤 按照属性 分类 品牌 价格区间 库存 排序 分页 高亮 聚合分析
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//构建DSL语句

        /**
         * 模糊匹配 过滤 按照属性 分类 品牌 价格区间 库存
         */
        //1 构建boolQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1 must 模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //1.2 bool = filter 按照3级分类id查询
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //1.2 bool = filter 按照品牌id查询
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        //1.2 bool = filter 按照所指的属性进行查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            //attrs=1_5寸:8寸&attrs=2_16G:8G
            for (String attrStr : param.getAttrs()) {
                BoolQueryBuilder nestBoolQuery = QueryBuilders.boolQuery();
                //attr = 1_5寸:8寸
                String[] s = attrStr.split("_");
                String attrId = s[0];//检索的属性id
                String[] attrValues = s[1].split(":");//检索的属性值
                nestBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //每一个都必须生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        //1.2 bool = filter 按照是否有库存进行查询
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        //1.2 bool = filter 按照价格区间进行查询
        //1_500   _500  500_
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                //区间
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    //_500
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    //500_
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        //把以前的所有条件都拿来进行封装
        sourceBuilder.query(boolQuery);

        /**
         * 排序 分页 高亮
         */
        //2.1 排序
        if (!StringUtils.isEmpty(param.getSort())) {
            //sort=saleCount_asc/desc 倒序
            String[] s = param.getSort().split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        //2.2 分页 每页5个，
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //2.3 高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }

        /**
         * 聚合分析
         */
        //3.1 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //子聚合 品牌
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName")).size(1);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg")).size(1);
        sourceBuilder.aggregation(brand_agg);

        //3.2 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        //子聚合
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);

        //3.3 属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //子聚合
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //子子聚合 2个
        //聚合分析出当前所有attrId对应的名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName")).size(1);
        //聚合分析出当前attrid对应的所有可能的属性值 attrvalue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue")).size(50);
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);


        String s = sourceBuilder.toString();
        System.out.println("构建的DSL。。。" + s);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }

    /**
     * 构建结果数据
     * 根据es查询到的结果，分析得到页面真正得到的数据模型
     * @param response
     * @param param
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {

        //要封装的大对象
        SearchResult result = new SearchResult();
        //1 封装返回的所有查询到的商品
        ArrayList<SkuEsModel> esModels = new ArrayList<>();
        SearchHits hits = response.getHits();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                //高亮
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);

        //2 当前所有商品涉及到的所有属性信息 Aggregation -> ParsedNested
        ArrayList<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        //nested的第一层 聚合 Aggregation -> ParsedLongTerms
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            //要封装的小对象
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //得到属性id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);
            //子聚合 得到属性名 Aggregation -> ParsedStringTerms
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();//因为这个属性不是List
            attrVo.setAttrName(attrName);
            //子聚合 复杂 得到属性值 Aggregation -> ParsedStringTerms
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attr_value_agg.getBuckets().stream().map((item) -> { //因为这个属性是List
                return item.getKeyAsString();
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        //3 当前所有商品所涉及的品牌信息 Aggregation -> ParsedLongTerms
        ArrayList<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            //要封装的小对象
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //得到品牌id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            //子聚合 得到品牌名 Aggregation -> ParsedStringTerms
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();//因为这个属性不是List
            brandVo.setBrandName(brandName);
            //子聚合 得到品牌图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();//因为这个属性不是List
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //4 当前所有商品所涉及到的所有分类信息 Aggregation -> ParsedLongTerms
        ArrayList<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            //要封装的小对象
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //子聚合 得到分类名 Aggregation -> ParsedStringTerms
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();//因为这个属性不是List
            catalogVo.setCatalogName(catalog_name);

            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        //5 分页信息 - 页码
        result.setPageNum(param.getPageNum());
        //5 分页信息 - 总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //5 分页信息 - 总页码 计算得到 11 / 2 = 5 ... 1
        int totalPages = (int)total % EsConstant.PRODUCT_PAGESIZE == 0?(int)total/EsConstant.PRODUCT_PAGESIZE:(int)(total/EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);

        //页码导航
        ArrayList<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        //6 构建面包屑导航功能 属性
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                //1 分析每个attrs传过来的查询参数值
                //attrs=2_5寸:6寸
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    //正常返回
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {});
                    navVo.setNavName(data.getAttrName());
                } else {
                    //如果失败
                    navVo.setNavName(s[0]);
                }

                //2 取消了面包屑以后 我们要跳转到哪个地方 将请求地址的url里面的当前请求参数置空
                //拿到所有的查询条件 去掉当前
                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
        }

        //品牌，分类 面包屑
        if(param.getBrandId() != null && param.getBrandId().size()>0) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            //TODO 远程查询
            R r = productFeignService.BrandsInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brands = r.getData("brand", new TypeReference<List<BrandVo>>() {});
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brands) {
                    buffer.append(brandVo.getName() + ";");
                    replace = replaceQueryString(param, brandVo.getBrandId()+"", "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }

        //返回这个大对象给前端
        return result;
    }

    //编写面包屑的功能时，删除指定请求
    private String replaceQueryString(SearchParam param, String value,String key) {
        String encode = "";
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            //+ 对应浏览器的%20编码
            encode = encode.replace("+","%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return  param.get_queryString().replace("&" + key + "=" + encode, "");
    }
}
