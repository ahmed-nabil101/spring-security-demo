package nobel.springsecurity.demo.security;

import nobel.springsecurity.demo.auth.ApplicationUserService;
import nobel.springsecurity.demo.jwt.JwtTokenVerifier;
import nobel.springsecurity.demo.jwt.JwtUsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final ApplicationUserService applicationUserService;

    public ApplicationSecurityConfig(PasswordEncoder passwordEncoder,
                                     ApplicationUserService applicationUserService) {
        this.passwordEncoder = passwordEncoder;
        this.applicationUserService = applicationUserService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /** Basic Authentication :
         * Basic authentication is a simple authentication scheme built into the HTTP protocol.
         * The client sends HTTP requests with the Authorization header that contains
         * the word Basic word followed by a space and a base64-encoded
            1- You cant logout while using Basic authentication.
            2- Each request, sends the username and password in the header of it.
         */
        http
//                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new JwtUsernamePasswordAuthenticationFilter(authenticationManager()))
                .addFilterAfter(new JwtTokenVerifier(), JwtUsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                /**
                 * antMatchers order matters, the first antMatcher that hits the api endpoint will return the boolean
                 * @PreAuthorize can be used at method level in the Controller class, but antMatchers is preferred
                 * */
                .antMatchers("/", "/index", "/css/*", "/js/*", "/login").permitAll()
//                .antMatchers("/students/**").hasRole(UserRole.ADMIN.name()) // Role based authentication
                .antMatchers(HttpMethod.POST, "/students/**").hasAuthority(UserPermission.STUDENT_WRITE.name())
                .antMatchers(HttpMethod.PUT, "/students/**").hasRole(UserRole.ADMIN.name())
                .antMatchers(HttpMethod.DELETE, "/students/**").hasRole(UserRole.ADMIN.name())
                .antMatchers(HttpMethod.GET, "/students/**").hasAnyRole(UserRole.ADMIN.name(), UserRole.ADMINTRAINEE.name())
                .anyRequest()
                .authenticated();

//                .and()
//                .formLogin()
//                    .loginPage("/login").permitAll()
//                    .usernameParameter("username")
//                    .passwordParameter("password")
//                    .defaultSuccessUrl("/students", true)
//                .and()
//                .rememberMe()
//                    .key("SomeSoStrongAndSecureKey")
//                    .tokenValiditySeconds((int)TimeUnit.DAYS.toSeconds(21))
//                    .rememberMeParameter("remember-me")
//                .and()
//                /**
//                 * Best practice is to use POST for any request that changes the state of application (i.e. logout)
//                 * to prevent CSRF attacks
//                 * */
//                .logout()
//                    .logoutUrl("/logout")
//                    .clearAuthentication(true)
//                    .invalidateHttpSession(true)
//                    .deleteCookies("JSESSIONID", "remember-me")
//                    .logoutSuccessUrl("/login");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(applicationUserService);
        return provider;
    }
}
