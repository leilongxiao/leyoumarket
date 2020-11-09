import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Spu;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class test {
    /**
     * StringUtils.join方法测试
     */
    @Test
    public void test1() {
//        List<Spu> spus = new ArrayList<>();
        Spu spu1 = new Spu();
        spu1.setCid1(0L);
        spu1.setCid2(1L);
        spu1.setCid3(2L);
        spu1.setTitle("aflds");

        Spu spu2 = new Spu();
        spu2.setCid1(5L);
        spu2.setCid2(6L);
        spu2.setCid3(7L);
        spu2.setTitle("fafga");
//        spus.add(spu1);
//        spus.add(spu1);
//        spus.add(spu2);
        List<String> names = new ArrayList<>();
        names.add(spu1.getTitle());
        names.add(spu2.getTitle());


        System.out.println(Arrays.asList(spu1.getCid1(), spu1.getCid2(), spu1.getCid3()));
        System.out.println(StringUtils.join(names, "/"));//测试工具的join方法的作用
    }

    /**
     * BeanUtils.copyProperties测试
     */
    @Test
    public void test2() {
        SpuBo spuBo = new SpuBo();
        spuBo.setCid1(0L);
        spuBo.setCid2(1L);
        spuBo.setCid3(2L);
        spuBo.setTitle("aflds");
        spuBo.setBname("数据佛");
//      spuBo.setLastUpdateTime(new Date());

        Spu spu = new Spu();
        BeanUtils.copyProperties(spuBo, spu);

        System.out.println(spu.toString());
    }

    /**
     *
     */
    @Test
    public void test3(){

    }
}
