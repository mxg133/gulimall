package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 品牌
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 10:36:07
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    //search调用的远程服务
    @GetMapping("/infos")
    public R BrandsInfo(@RequestParam("brandIds") List<Long> brandIds){
        List<BrandEntity> brands = brandService.getBrandsByIds(brandIds);

        return R.ok().put("brand", brands);
    }

    /**
     * 保存
     * @Valid 告诉MVC 这个需要校验
     * BindingResult 保存了校验结果
     * @Validated(AddGroup.class)
     */
    @RequestMapping("/save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand/*, BindingResult result*/){
//        if (result.hasErrors()) {
//            Map<String, String> map = new HashMap<>();
//            //获取校验的结果
//            result.getFieldErrors().forEach((item) -> {
//                //FieldError 获取到错误提示
//                String message = item.getDefaultMessage();
//                //获取错误的属名字
//                String field = item.getField();
//                map.put(field, message);
//            });
//            return R.error(400, "提交的数据不合法...").put("data", map);
//        } else {
//        }
		    brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改所有信息
     */
    @RequestMapping("/update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
