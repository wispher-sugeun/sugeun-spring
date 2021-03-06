package com.jamsil_team.sugeun.config;

import com.jamsil_team.sugeun.domain.user.UserRepository;
import com.jamsil_team.sugeun.filter.ApiCheckFilter;
import com.jamsil_team.sugeun.filter.ApiLoginFilter;
import com.jamsil_team.sugeun.security.util.JWTUtil;
import com.jamsil_team.sugeun.service.folder.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired private UserRepository userRepository;

    @Bean
    public PasswordEncoder encode(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JWTUtil jwtUtil(){
        return new JWTUtil();
    }

    @Bean
    public ApiLoginFilter apiLoginFilter() throws Exception{
        ApiLoginFilter apiLoginFilter = new ApiLoginFilter("/api/login", jwtUtil());
        apiLoginFilter.setAuthenticationManager(authenticationManager());

        return apiLoginFilter;

    }


    @Bean
    public ApiCheckFilter apiCheckFilter(){
        return new ApiCheckFilter("/api/**/*", jwtUtil(), userRepository);
    }


    @Override
    protected  void configure(HttpSecurity http) throws Exception{
        http.httpBasic().disable(); // rest api 이므로 기본설정 안함. 기본설정은 비인증시 로그인폼 화면으로 리다이렉트

        //세션 사용 x (
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.csrf().disable(); //세션 사용 x

        http.authorizeRequests()
                .antMatchers("/users/**","/phrases/**","/links/**","/timeouts/**")
                .access("hasRole('ROLE_USER')")
                .anyRequest().permitAll();

        http.addFilterBefore(apiCheckFilter(),
                UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(apiLoginFilter(),
                UsernamePasswordAuthenticationFilter.class); //로그인 필터를  인증필터 전에 실행
    }

}
