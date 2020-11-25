package com.leyou.goods.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;

@Service
public class GoodsHtmlService {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private TemplateEngine templateEngine;

//    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsHtmlService.class);

    /**
     * 创建html页面
     *
     * @param spuId
     * @throws Exception
     */
    public void createHtml(Long spuId) {

        PrintWriter writer = null;
        try {
            // 获取页面数据

            // 创建thymeleaf上下文对象
            Context context = new Context();
            // 把数据放入上下文对象
            context.setVariables(this.goodsService.loadData(spuId));

            // 创建输出流
            File file = new File("D:\\1leyoustart\\nginx-1.14.0\\html\\item\\" + spuId + ".html");
            writer = new PrintWriter(file);

            // 执行页面静态化方法
            this.templateEngine.process("item", context, writer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public void deleteHtml(Long spuId) {
        File file = new File("D:\\1leyoustart\\nginx-1.14.0\\html\\item\\" + spuId + ".html");
        file.deleteOnExit();
    }


}