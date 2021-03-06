package com.cory.web.config;

import com.cory.web.captcha.CoryCacheCaptchaStore;
import com.cory.web.captcha.JcaptchaServlet;
import com.cory.web.filter.CaptchaFilter;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.color.SingleColorGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.DecoratedRandomTextPaster;
import com.octo.captcha.component.image.textpaster.textdecorator.BaffleTextDecorator;
import com.octo.captcha.component.image.wordtoimage.ComposedWordToImage;
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator;
import com.octo.captcha.engine.GenericCaptchaEngine;
import com.octo.captcha.image.gimpy.GimpyFactory;
import com.octo.captcha.service.captchastore.CaptchaStore;
import com.octo.captcha.service.multitype.GenericManageableCaptchaService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.*;

import static com.cory.web.config.Constant.CAPTCHA_URL;

/**
 * Created by Cory on 2021/2/9.
 */
@EnableConfigurationProperties(CaptchaProperties.class)
@Configuration
public class CaptchaConfig {

    @Bean
    public RandomWordGenerator wordgen() {
        return new RandomWordGenerator("aabbccddeefgghhkkmnnooppqqsstuuvvwxxyyzz");
    }

    @Bean
    public RandomFontGenerator fontGenRandom() {
        Font font = new Font("Arial", 0, 26);
        return new RandomFontGenerator(20, 28, new Font[] {font});
    }

    @Bean
    public UniColorBackgroundGenerator backGenUni() {
        return new UniColorBackgroundGenerator(110, 40);
    }

    @Bean
    public Color colorWhite() {
        return new Color(255, 255, 255);
    }

    @Bean
    public Color colorBlack() {
        return new Color(50, 50, 50);
    }

    @Bean
    public SingleColorGenerator colorGen(Color colorBlack) {
        return new SingleColorGenerator(colorBlack);
    }

    @Bean
    public BaffleTextDecorator baffleDecorator(Color colorWhite) {
        return new BaffleTextDecorator(1, colorWhite);
    }

    @Bean
    public DecoratedRandomTextPaster decoratedPaster(SingleColorGenerator colorGen) {
        return new DecoratedRandomTextPaster(4, 4, colorGen, null);
    }

    @Bean
    public ComposedWordToImage wordtoimage(RandomFontGenerator fontGenRandom, UniColorBackgroundGenerator backGenUni, DecoratedRandomTextPaster decoratedPaster) {
        return new ComposedWordToImage(fontGenRandom, backGenUni, decoratedPaster);
    }

    @Bean
    public GimpyFactory captchaFactory(RandomWordGenerator wordgen, ComposedWordToImage wordtoimage) {
        return new GimpyFactory(wordgen, wordtoimage);
    }

    @Bean
    public GenericCaptchaEngine imageEngine(GimpyFactory captchaFactory) {
        return new GenericCaptchaEngine(new GimpyFactory[] {captchaFactory});
    }

    @Bean
    public GenericManageableCaptchaService captchaService(GenericCaptchaEngine imageEngine, CacheManager cacheManager) {
        CaptchaStore store = new CoryCacheCaptchaStore(cacheManager);
        return new GenericManageableCaptchaService(store, imageEngine, 180, 100000, 75000);
    }

    //??????Servlet
    @Bean
    public ServletRegistrationBean myServlet(){
        return new ServletRegistrationBean(new JcaptchaServlet(), CAPTCHA_URL);
    }

    //??????Filter
    @Bean
    public FilterRegistrationBean captchaFilterRegistration(CaptchaFilter captchaFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(captchaFilter);
        //registration.addUrlPatterns("/*");
        // ???????????????false,?????????????????????SpringApplicationContext??????,?????????true????????????ServletContainer??????
        //registration.setEnabled(false);
        registration.setName("captchaFilter");
        registration.setOrder(1);
        return registration;
    }
}
