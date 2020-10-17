package com.leyou.item.controller;

import com.leyou.item.pojo.Brand;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("brand")
public class BrandController {
    @GetMapping("page")
    public List<Brand> addBrand( ) {
        Brand brand = new Brand();
//        brand.setId();
//        brand.setImage();
//        brand.setLetter();
//        brand.setName();


        return null;
    }

}
