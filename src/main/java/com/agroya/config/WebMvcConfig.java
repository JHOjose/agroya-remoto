package com.agroya.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Configuración MVC: internacionalización (i18n) y rutas de recursos estáticos.
 * El parámetro ?lang=es|en|fr cambia el idioma y lo guarda en sesión.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Resuelve el locale del usuario guardándolo en la sesión HTTP.
     * Por defecto usa español (Colombia).
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(new Locale("es"));
        return slr;
    }

    /**
     * Interceptor que detecta el parámetro ?lang=XX en cualquier petición
     * y actualiza el locale en sesión.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    /**
     * Fuente de mensajes i18n. Lee los archivos:
     *   src/main/resources/messages.properties       (español, por defecto)
     *   src/main/resources/messages_en.properties    (inglés)
     *   src/main/resources/messages_fr.properties    (francés)
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        return ms;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}