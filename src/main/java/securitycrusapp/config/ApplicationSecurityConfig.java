package securitycrusapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import securitycrusapp.config.handler.CustomAccessDeniedHandler;
import securitycrusapp.config.handler.CustomAuthenticationFailureHandler;
import securitycrusapp.config.handler.CustomAuthenticationSuccessHandler;
import securitycrusapp.config.handler.CustomUrlLogoutSuccessHandler;
import securitycrusapp.service.AppService;

@Configuration
@EnableWebSecurity
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    // сервис, с помощью которого тащим пользователя
    private final AppService appService;

    private final PasswordEncoder passwordEncoder;

    // класс, в котором описана логика перенаправления пользователей по ролям
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    // класс, в котором описана логика при неудачной авторизации
    private final CustomAuthenticationFailureHandler authenticationFailureHandler;

    // класс, в котором описана логика при удачной авторизации
    private final CustomUrlLogoutSuccessHandler urlLogoutSuccessHandler;

    // класс, в котором описана логика при отказе в доступе
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    public ApplicationSecurityConfig(AppService appServiceTmp,
                                     PasswordEncoder passwordEncoder,
                                     CustomAuthenticationSuccessHandler authenticationSuccessHandler,
                                     CustomAuthenticationFailureHandler authenticationFailureHandler,
                                     CustomUrlLogoutSuccessHandler urlLogoutSuccessHandler,
                                     CustomAccessDeniedHandler accessDeniedHandler) {
        this.appService = appServiceTmp;
        this.passwordEncoder = passwordEncoder;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.urlLogoutSuccessHandler = urlLogoutSuccessHandler;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(appService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "index", "/css/**", "/js/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler);
        http.formLogin()
                .loginPage("/") // указываем страницу с формой логина
                .permitAll()  // даем доступ к форме логина всем
                .successHandler(authenticationSuccessHandler) //указываем логику обработки при удачном логине
                .failureHandler(authenticationFailureHandler) //указываем логику обработки при неудачном логине
                .usernameParameter("email") // Указываем параметры логина и пароля с формы логина
                .passwordParameter("password");
        http.logout()
                .logoutUrl("/logout")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/?logout")
                .logoutSuccessHandler(urlLogoutSuccessHandler)
                .permitAll();
    }
}
