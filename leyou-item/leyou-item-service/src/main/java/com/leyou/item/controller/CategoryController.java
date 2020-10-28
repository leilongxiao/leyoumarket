package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父id查询所有子节点
     *
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoriesByPid(@RequestParam(value = "pid", defaultValue = "0") Long pid) {
        try {
        /*
        判断pid是否合法,不合法返回客户端错误请求状态码400
         */
            if (pid == null || pid < 0) {
                //return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                return ResponseEntity.badRequest().build();
            }
            //执行查询获取结果集
            List<Category> categories = this.categoryService.queryCategoriesByPid(pid);
            //为空，响应404
            if (CollectionUtils.isEmpty(categories)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            //正常，响应200
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //服务器异常，响应500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    //-------------------------------------------节点 增删改

    /**
     * 添加一个节点
     *
     * @param name
     * @param parentId
     * @param isParent
     * @param sort
     */
    @PostMapping
    public ResponseEntity<Void> categoryAdd(@RequestParam(name = "id", required = false) Long id,
                                            @RequestParam(name = "name", required = true) String name,
                                            @RequestParam(name = "parentId", required = true) Long parentId,
                                            @RequestParam(name = "isParent", required = true) Boolean isParent,
                                            @RequestParam(name = "sort", required = true) Integer sort) {
        this.categoryService.categoryAdd(name, parentId, isParent, sort);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> categoryEdit(@RequestParam(name = "id", required = false) Long id,
                                             @RequestParam(name = "name", required = true) String name) {
        this.categoryService.categoryEdit(id,name);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> categoryDelete(@RequestParam(name = "id", required = true) Long id){
        this.categoryService.categoryDelete(id);
        return ResponseEntity.ok().build();
    }
}
