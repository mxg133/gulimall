package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 属性分组
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-21 11:53:38
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    AttrService attrService;

    @Autowired
    AttrAttrgroupRelationService relationService;

    //product/attrgroup/{attrgroupId}/attr/relation
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long catelogId){
        List<AttrEntity> entities = attrService.getRelationAttr(catelogId);
        return R.ok().put("data", entities);
    }

    //product/attrgroup/{attrgroupId}/noattr/relation
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(
            @RequestParam Map<String, Object> params,
            @PathVariable("attrgroupId") Long attrgroupId){
        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);
        return R.ok().put("page", page);
    }

    //product/attrgroup/attr/relation
    @PostMapping("attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        relationService.saveBatch(vos);
        return R.ok();
    }

    //17、获取分类下所有分组&关联属性
    //product/attrgroup/{catelogId}/withattr
    @GetMapping("/{catelogId}/withattr")
    public R attrGroupWithAttrs(@PathVariable("catelogId") Long catelogId) {
        //1、查出当前分类下的所有属性分组，
        //2、查出每个属性分组的所有属性
        List<AttrGroupWithAttrsVo> vos =  attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data",vos);
    }


    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long path[] = categoryService.findCatelogPath(catelogId);

        attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 删除 属性 与 关联关系
     */
    //product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo vos[]){
        attrService.deleteRelation(vos);

        return R.ok();
    }

}
