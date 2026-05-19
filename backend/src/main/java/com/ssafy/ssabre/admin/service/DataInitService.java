package com.ssafy.ssabre.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataInitService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public int initBoards() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM boards", Long.class);
        if (count != null && count > 0) {
            log.info("Boards 테이블에 이미 {}건의 데이터가 있습니다. 초기화를 건너뜁니다.", count);
            return 0;
        }

        List<Object[]> boards = List.of(
                new Object[]{"자유게시판", "GENERAL", "자유롭게 소통하는 공간입니다."},
                new Object[]{"서울 캠퍼스 게시판", "GENERAL", "서울 캠퍼스 교육생들을 위한 게시판입니다."},
                new Object[]{"대전 캠퍼스 게시판", "GENERAL", "대전 캠퍼스 교육생들을 위한 게시판입니다."},
                new Object[]{"광주 캠퍼스 게시판", "GENERAL", "광주 캠퍼스 교육생들을 위한 게시판입니다."},
                new Object[]{"구미 캠퍼스 게시판", "GENERAL", "구미 캠퍼스 교육생들을 위한 게시판입니다."},
                new Object[]{"부울경 캠퍼스 게시판", "GENERAL", "부울경 캠퍼스 교육생들을 위한 게시판입니다."},
                new Object[]{"프로젝트 홍보 게시판", "GENERAL", "프로젝트를 홍보하는 게시판입니다."},
                new Object[]{"정보 게시판", "GENERAL", "유용한 정보를 공유하는 게시판입니다."},
                new Object[]{"싸탈 게시판", "GENERAL", "SSAFY 퇴소 후 정보를 나누는 게시판입니다."},
                new Object[]{"채용 게시판", "GENERAL", "채용 정보를 공유하는 게시판입니다."},
                new Object[]{"질문 게시판", "GENERAL", "궁금한 점을 질문하고 답변을 받는 게시판입니다."},
                new Object[]{"장터 게시판", "GENERAL", "중고 물품을 사고파는 게시판입니다."},
                new Object[]{"이벤트 게시판", "GENERAL", "각종 이벤트 정보를 공유하는 게시판입니다."}
        );
        String sql = "INSERT INTO boards (name, category, description) VALUES (?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, boards);
        log.info("Boards 초기화 완료: {}건", boards.size());
        return boards.size();
    }

    @Transactional
    public int initCampuses() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM campuses", Long.class);
        if (count != null && count > 0) {
            log.info("Campuses 테이블에 이미 {}건의 데이터가 있습니다. 초기화를 건너뜁니다.", count);
            return 0;
        }

        List<Object[]> campuses = List.of(
                new Object[]{1, "서울"},
                new Object[]{2, "대전"},
                new Object[]{3, "광주"},
                new Object[]{4, "구미"},
                new Object[]{5, "부울경"}
        );
        String sql = "INSERT INTO campuses (id, name) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, campuses);
        log.info("Campuses 초기화 완료: {}건", campuses.size());
        return campuses.size();
    }

    @Transactional
    public int initClasses() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM classes", Long.class);
        if (count != null && count > 0) {
            log.info("Classes 테이블에 이미 {}건의 데이터가 있습니다. 초기화를 건너뜁니다.", count);
            return 0;
        }

        List<Object[]> classes = getClassData();
        String sql = "INSERT INTO classes (class_no, generation, campus_id, id, track_type, name) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, classes);
        log.info("Classes 초기화 완료: {}건", classes.size());
        return classes.size();
    }

    @Transactional
    public int initStacks() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stacks", Long.class);
        if (count != null && count > 0) {
            log.info("Stacks 테이블에 이미 {}건의 데이터가 있습니다. 초기화를 건너뜁니다.", count);
            return 0;
        }

        List<Object[]> stacks = getStackData();
        String sql = "INSERT INTO stacks (name, img_url) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, stacks);
        log.info("Stacks 초기화 완료: {}건", stacks.size());
        return stacks.size();
    }

    private List<Object[]> getClassData() {
        // class_no, generation, campus_id, id, track_type, name
        return List.of(
                // 14기 데이터
                new Object[]{1, 14, 1, 1, "비전공", "1반"},
                new Object[]{2, 14, 1, 2, "비전공", "2반"},
                new Object[]{3, 14, 1, 3, "비전공", "3반"},
                new Object[]{4, 14, 1, 4, "비전공", "4반"},
                new Object[]{5, 14, 1, 5, "비전공", "5반"},
                new Object[]{6, 14, 1, 6, "비전공", "6반"},
                new Object[]{7, 14, 1, 7, "전공", "7반"},
                new Object[]{8, 14, 1, 8, "전공", "8반"},
                new Object[]{9, 14, 1, 9, "전공", "9반"},
                new Object[]{10, 14, 1, 10, "전공", "10반"},
                new Object[]{11, 14, 1, 11, "전공", "11반"},
                new Object[]{12, 14, 1, 12, "전공", "12반"},
                new Object[]{13, 14, 1, 13, "비전공", "13반"},
                new Object[]{14, 14, 1, 14, "비전공", "14반"},
                new Object[]{15, 14, 1, 15, "비전공", "15반"},
                new Object[]{16, 14, 1, 16, "비전공", "16반"},
                new Object[]{17, 14, 1, 17, "비전공", "17반"},
                new Object[]{18, 14, 1, 18, "비전공", "18반"},
                new Object[]{19, 14, 1, 19, "전공", "19반"},
                new Object[]{20, 14, 1, 20, "전공", "20반"},
                new Object[]{1, 14, 2, 21, "비전공", "1반"},
                new Object[]{2, 14, 2, 22, "비전공", "2반"},
                new Object[]{3, 14, 2, 23, "비전공", "3반"},
                new Object[]{4, 14, 2, 24, "전공", "4반"},
                new Object[]{5, 14, 2, 25, "전공", "5반"},
                new Object[]{6, 14, 2, 26, "전공", "6반"},
                new Object[]{1, 14, 3, 27, "비전공", "1반"},
                new Object[]{2, 14, 3, 28, "비전공", "2반"},
                new Object[]{3, 14, 3, 29, "전공", "3반"},
                new Object[]{4, 14, 3, 30, "전공", "4반"},
                new Object[]{5, 14, 3, 31, "전공", "5반"},
                new Object[]{1, 14, 4, 32, "비전공", "1반"},
                new Object[]{2, 14, 4, 33, "비전공", "2반"},
                new Object[]{3, 14, 4, 34, "비전공", "3반"},
                new Object[]{4, 14, 4, 35, "전공", "4반"},
                new Object[]{5, 14, 4, 36, "전공", "5반"},
                new Object[]{6, 14, 4, 37, "전공", "6반"},
                new Object[]{1, 14, 5, 38, "비전공", "1반"},
                new Object[]{2, 14, 5, 39, "비전공", "2반"},
                new Object[]{3, 14, 5, 40, "전공", "3반"},
                new Object[]{4, 14, 5, 41, "전공", "4반"},

                // 15기 서울 (1~6: 비전공, 7~14: 전공, 15~18: 비전공, 19~20: 전공)
                new Object[]{1, 15, 1, 42, "비전공", "1반"},
                new Object[]{2, 15, 1, 43, "비전공", "2반"},
                new Object[]{3, 15, 1, 44, "비전공", "3반"},
                new Object[]{4, 15, 1, 45, "비전공", "4반"},
                new Object[]{5, 15, 1, 46, "비전공", "5반"},
                new Object[]{6, 15, 1, 47, "비전공", "6반"},
                new Object[]{7, 15, 1, 48, "전공", "7반"},
                new Object[]{8, 15, 1, 49, "전공", "8반"},
                new Object[]{9, 15, 1, 50, "전공", "9반"},
                new Object[]{10, 15, 1, 51, "전공", "10반"},
                new Object[]{11, 15, 1, 52, "전공", "11반"},
                new Object[]{12, 15, 1, 53, "전공", "12반"},
                new Object[]{13, 15, 1, 54, "전공", "13반"},
                new Object[]{14, 15, 1, 55, "전공", "14반"},
                new Object[]{15, 15, 1, 56, "비전공", "15반"},
                new Object[]{16, 15, 1, 57, "비전공", "16반"},
                new Object[]{17, 15, 1, 58, "비전공", "17반"},
                new Object[]{18, 15, 1, 59, "비전공", "18반"},
                new Object[]{19, 15, 1, 60, "전공", "19반"},
                new Object[]{20, 15, 1, 61, "전공", "20반"},

                // 15기 대전 (1,2,5: 비전공, 3,4,6: 전공)
                new Object[]{1, 15, 2, 62, "비전공", "1반"},
                new Object[]{2, 15, 2, 63, "비전공", "2반"},
                new Object[]{3, 15, 2, 64, "전공", "3반"},
                new Object[]{4, 15, 2, 65, "전공", "4반"},
                new Object[]{5, 15, 2, 66, "비전공", "5반"},
                new Object[]{6, 15, 2, 67, "전공", "6반"},

                // 15기 광주 (1: 비전공, 2~4: 전공)
                new Object[]{1, 15, 3, 68, "비전공", "1반"},
                new Object[]{2, 15, 3, 69, "전공", "2반"},
                new Object[]{3, 15, 3, 70, "전공", "3반"},
                new Object[]{4, 15, 3, 71, "전공", "4반"},

                // 15기 구미 (1,2: 비전공, 3,4,5: 전공)
                new Object[]{1, 15, 4, 72, "비전공", "1반"},
                new Object[]{2, 15, 4, 73, "비전공", "2반"},
                new Object[]{3, 15, 4, 74, "전공", "3반"},
                new Object[]{4, 15, 4, 75, "전공", "4반"},
                new Object[]{5, 15, 4, 76, "전공", "5반"},

                // 15기 부울경 (1,2: 비전공, 3,4: 전공)
                new Object[]{1, 15, 5, 77, "비전공", "1반"},
                new Object[]{2, 15, 5, 78, "비전공", "2반"},
                new Object[]{3, 15, 5, 79, "전공", "3반"},
                new Object[]{4, 15, 5, 80, "전공", "4반"}
        );
    }

    private List<Object[]> getStackData() {
        return List.of(
                // Programming Languages
                new Object[]{"C", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/c/c-original.svg"},
                new Object[]{"C++", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/cplusplus/cplusplus-original.svg"},
                new Object[]{"C#", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/csharp/csharp-original.svg"},
                new Object[]{"Java", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg"},
                new Object[]{"Python", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/python/python-original.svg"},
                new Object[]{"JavaScript", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/javascript/javascript-original.svg"},
                new Object[]{"TypeScript", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/typescript/typescript-original.svg"},
                new Object[]{"Go", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/go/go-original.svg"},
                new Object[]{"Rust", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/rust/rust-original.svg"},
                new Object[]{"Swift", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/swift/swift-original.svg"},
                new Object[]{"Kotlin", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kotlin/kotlin-original.svg"},
                new Object[]{"PHP", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/php/php-original.svg"},
                new Object[]{"Ruby", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/ruby/ruby-original.svg"},
                new Object[]{"Scala", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/scala/scala-original.svg"},
                new Object[]{"Haskell", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/haskell/haskell-original.svg"},
                new Object[]{"Lua", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/lua/lua-original.svg"},
                new Object[]{"Perl", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/perl/perl-original.svg"},
                new Object[]{"R", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/r/r-original.svg"},
                new Object[]{"Dart", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/dart/dart-original.svg"},
                new Object[]{"Solidity", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/solidity/solidity-original.svg"},
                new Object[]{"Shell Script", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/bash/bash-original.svg"},
                new Object[]{"PowerShell", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/powershell/powershell-original.svg"},
                new Object[]{"Objective-C", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/objectivec/objectivec-plain.svg"},
                new Object[]{"Assembly", null},
                new Object[]{"Matlab", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/matlab/matlab-original.svg"},
                new Object[]{"Groovy", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/groovy/groovy-original.svg"},
                new Object[]{"Elixir", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/elixir/elixir-original.svg"},
                new Object[]{"Clojure", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/clojure/clojure-original.svg"},
                new Object[]{"F#", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/fsharp/fsharp-original.svg"},
                new Object[]{"OCaml", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/ocaml/ocaml-original.svg"},
                new Object[]{"Julia", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/julia/julia-original.svg"},
                new Object[]{"V", null},
                new Object[]{"Erlang", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/erlang/erlang-original.svg"},
                new Object[]{"Fortran", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/fortran/fortran-original.svg"},
                new Object[]{"Cobol", null},
                new Object[]{"Ada", null},
                new Object[]{"Pascal", null},
                new Object[]{"Racket", null},
                new Object[]{"Scheme", null},

                // Frontend
                new Object[]{"HTML5", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/html5/html5-original.svg"},
                new Object[]{"CSS3", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/css3/css3-original.svg"},
                new Object[]{"React", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original.svg"},
                new Object[]{"Vue.js", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/vuejs/vuejs-original.svg"},
                new Object[]{"Angular", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/angularjs/angularjs-original.svg"},
                new Object[]{"Svelte", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/svelte/svelte-original.svg"},
                new Object[]{"Next.js", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/nextjs/nextjs-original.svg"},
                new Object[]{"Nuxt.js", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/nuxtjs/nuxtjs-original.svg"},
                new Object[]{"jQuery", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/jquery/jquery-original.svg"},
                new Object[]{"Bootstrap", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/bootstrap/bootstrap-original.svg"},
                new Object[]{"Tailwind CSS", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/tailwindcss/tailwindcss-original.svg"},
                new Object[]{"Sass", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/sass/sass-original.svg"},
                new Object[]{"Less", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/less/less-plain-wordmark.svg"},
                new Object[]{"Stylus", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/stylus/stylus-original.svg"},
                new Object[]{"Webpack", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/webpack/webpack-original.svg"},
                new Object[]{"Vite", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/vitejs/vitejs-original.svg"},
                new Object[]{"Parcel", null},
                new Object[]{"Babel", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/babel/babel-original.svg"},
                new Object[]{"Rollup", null},
                new Object[]{"Redux", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/redux/redux-original.svg"},
                new Object[]{"Recoil", null},
                new Object[]{"MobX", null},
                new Object[]{"Zustand", null},
                new Object[]{"Jotai", null},
                new Object[]{"React Query", null},
                new Object[]{"Axios", null},
                new Object[]{"Three.js", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/threejs/threejs-original.svg"},
                new Object[]{"D3.js", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/d3js/d3js-original.svg"},
                new Object[]{"Chart.js", null},
                new Object[]{"Canvas API", null},
                new Object[]{"WebGL", null},
                new Object[]{"WebAssembly", null},
                new Object[]{"PWA", null},
                new Object[]{"Electron", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/electron/electron-original.svg"},
                new Object[]{"Ionic", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/ionic/ionic-original.svg"},
                new Object[]{"Gatsby", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/gatsby/gatsby-original.svg"},
                new Object[]{"Hugo", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/hugo/hugo-original.svg"},
                new Object[]{"Jekyll", null},
                new Object[]{"Storybook", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/storybook/storybook-original.svg"},
                new Object[]{"Chakra UI", null},
                new Object[]{"Material UI", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/materialui/materialui-original.svg"},
                new Object[]{"Ant Design", null},
                new Object[]{"Framer Motion", null},

                // Backend
                new Object[]{"Spring", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg"},
                new Object[]{"Spring Boot", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg"},
                new Object[]{"Node.js", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/nodejs/nodejs-original.svg"},
                new Object[]{"Express.js", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/express/express-original.svg"},
                new Object[]{"NestJS", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/nestjs/nestjs-original.svg"},
                new Object[]{"Koa.js", null},
                new Object[]{"Django", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/django/django-plain.svg"},
                new Object[]{"Flask", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/flask/flask-original.svg"},
                new Object[]{"FastAPI", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/fastapi/fastapi-original.svg"},
                new Object[]{"Tornado", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/tornado/tornado-original.svg"},
                new Object[]{"Ruby on Rails", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/rails/rails-plain.svg"},
                new Object[]{"ASP.NET", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/dot-net/dot-net-original.svg"},
                new Object[]{"ASP.NET Core", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/dotnetcore/dotnetcore-original.svg"},
                new Object[]{"Laravel", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/laravel/laravel-original.svg"},
                new Object[]{"Symfony", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/symfony/symfony-original.svg"},
                new Object[]{"CodeIgniter", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/codeigniter/codeigniter-plain.svg"},
                new Object[]{"CakePHP", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/cakephp/cakephp-original.svg"},
                new Object[]{"Ktor", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/ktor/ktor-original.svg"},
                new Object[]{"Gin", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/go/go-original.svg"},
                new Object[]{"Echo", null},
                new Object[]{"Fiber", null},
                new Object[]{"Revel", null},
                new Object[]{"Phoenix", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/phoenix/phoenix-original.svg"},
                new Object[]{"GraphQL", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/graphql/graphql-plain.svg"},
                new Object[]{"Apollo Server", null},
                new Object[]{"Prisma", null},
                new Object[]{"TypeORM", null},
                new Object[]{"Sequelize", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/sequelize/sequelize-original.svg"},
                new Object[]{"Hibernate", null},
                new Object[]{"JPA", null},
                new Object[]{"MyBatis", null},
                new Object[]{"gRPC", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/grpc/grpc-original.svg"},
                new Object[]{"Socket.io", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/socketio/socketio-original.svg"},
                new Object[]{"Kafka", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/apachekafka/apachekafka-original.svg"},
                new Object[]{"RabbitMQ", null},
                new Object[]{"ActiveMQ", null},
                new Object[]{"ZeroMQ", null},
                new Object[]{"Nginx", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/nginx/nginx-original.svg"},
                new Object[]{"Apache HTTP Server", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/apache/apache-original.svg"},
                new Object[]{"Tomcat", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/tomcat/tomcat-original.svg"},
                new Object[]{"Jetty", null},
                new Object[]{"Undertow", null},

                // Mobile
                new Object[]{"Flutter", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/flutter/flutter-original.svg"},
                new Object[]{"React Native", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original.svg"},
                new Object[]{"SwiftUI", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/swift/swift-original.svg"},
                new Object[]{"UIKit", null},
                new Object[]{"Jetpack Compose", null},
                new Object[]{"Xamarin", null},
                new Object[]{"Cordova", null},
                new Object[]{"PhoneGap", null},
                new Object[]{"Expo", null},
                new Object[]{"Realm", null},
                new Object[]{"SQLite", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/sqlite/sqlite-original.svg"},
                new Object[]{"CocoaPods", null},
                new Object[]{"Gradle", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/gradle/gradle-original.svg"},
                new Object[]{"Maven", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/maven/maven-original.svg"},
                new Object[]{"Fastlane", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/fastlane/fastlane-original.svg"},

                // Database
                new Object[]{"MySQL", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg"},
                new Object[]{"PostgreSQL", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg"},
                new Object[]{"Oracle Database", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/oracle/oracle-original.svg"},
                new Object[]{"MariaDB", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mariadb/mariadb-original.svg"},
                new Object[]{"MS SQL Server", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/microsoftsqlserver/microsoftsqlserver-plain.svg"},
                new Object[]{"MongoDB", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mongodb/mongodb-original.svg"},
                new Object[]{"Cassandra", null},
                new Object[]{"DynamoDB", null},
                new Object[]{"Firebase Realtime Database", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/firebase/firebase-plain.svg"},
                new Object[]{"Cloud Firestore", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/firebase/firebase-plain.svg"},
                new Object[]{"H2", null},
                new Object[]{"Redis", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/redis/redis-original.svg"},
                new Object[]{"Memcached", null},
                new Object[]{"Elasticsearch", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/elasticsearch/elasticsearch-original.svg"},
                new Object[]{"Solr", null},
                new Object[]{"Neo4j", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/neo4j/neo4j-original.svg"},
                new Object[]{"CouchDB", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/couchdb/couchdb-original.svg"},
                new Object[]{"Couchbase", null},
                new Object[]{"InfluxDB", null},
                new Object[]{"TimescaleDB", null},
                new Object[]{"CockroachDB", null},
                new Object[]{"TiDB", null},
                new Object[]{"Supabase", null},
                new Object[]{"PlanetScale", null},
                new Object[]{"Amazon Aurora", null},
                new Object[]{"HBase", null},
                new Object[]{"ArangoDB", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/arangodb/arangodb-original.svg"},
                new Object[]{"RocksDB", null},

                // Cloud & DevOps
                new Object[]{"AWS", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/amazonwebservices/amazonwebservices-original-wordmark.svg"},
                new Object[]{"Microsoft Azure", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/azure/azure-original.svg"},
                new Object[]{"Google Cloud Platform", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/googlecloud/googlecloud-original.svg"},
                new Object[]{"Docker", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg"},
                new Object[]{"Kubernetes", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kubernetes/kubernetes-original.svg"},
                new Object[]{"Docker Swarm", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg"},
                new Object[]{"Jenkins", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/jenkins/jenkins-original.svg"},
                new Object[]{"GitLab CI/CD", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/gitlab/gitlab-original.svg"},
                new Object[]{"GitHub Actions", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/github/github-original.svg"},
                new Object[]{"Travis CI", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/travis/travis-plain.svg"},
                new Object[]{"CircleCI", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/circleci/circleci-plain.svg"},
                new Object[]{"Bamboo", null},
                new Object[]{"TeamCity", null},
                new Object[]{"Ansible", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/ansible/ansible-original.svg"},
                new Object[]{"Terraform", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/terraform/terraform-original.svg"},
                new Object[]{"Puppet", null},
                new Object[]{"Chef", null},
                new Object[]{"Vagrant", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/vagrant/vagrant-original.svg"},
                new Object[]{"Packer", null},
                new Object[]{"Prometheus", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/prometheus/prometheus-original.svg"},
                new Object[]{"Grafana", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/grafana/grafana-original.svg"},
                new Object[]{"ELK Stack", null},
                new Object[]{"Datadog", null},
                new Object[]{"Splunk", null},
                new Object[]{"New Relic", null},
                new Object[]{"Sentry", null},
                new Object[]{"ArgoCD", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/argocd/argocd-original.svg"},
                new Object[]{"Helm", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/helm/helm-original.svg"},
                new Object[]{"Istio", null},
                new Object[]{"Linkerd", null},
                new Object[]{"Consul", null},
                new Object[]{"Vault", null},
                new Object[]{"HAProxy", null},
                new Object[]{"Cloudflare", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/cloudflare/cloudflare-original.svg"},
                new Object[]{"Heroku", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/heroku/heroku-original.svg"},
                new Object[]{"Vercel", null},
                new Object[]{"Netlify", null},
                new Object[]{"DigitalOcean", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/digitalocean/digitalocean-original.svg"},
                new Object[]{"Linode", null},

                // AI & Data Science
                new Object[]{"TensorFlow", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/tensorflow/tensorflow-original.svg"},
                new Object[]{"PyTorch", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/pytorch/pytorch-original.svg"},
                new Object[]{"Keras", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/keras/keras-original.svg"},
                new Object[]{"Scikit-learn", null},
                new Object[]{"Pandas", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/pandas/pandas-original.svg"},
                new Object[]{"NumPy", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/numpy/numpy-original.svg"},
                new Object[]{"Matplotlib", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/matplotlib/matplotlib-original.svg"},
                new Object[]{"Seaborn", null},
                new Object[]{"SciPy", null},
                new Object[]{"Jupyter Notebook", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/jupyter/jupyter-original.svg"},
                new Object[]{"OpenCV", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/opencv/opencv-original.svg"},
                new Object[]{"Hugging Face Transformers", null},
                new Object[]{"NLTK", null},
                new Object[]{"Spacy", null},
                new Object[]{"Apache Spark", null},
                new Object[]{"Hadoop", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/hadoop/hadoop-original.svg"},
                new Object[]{"Hive", null},
                new Object[]{"Pig", null},
                new Object[]{"Kafka Streams", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/apachekafka/apachekafka-original.svg"},
                new Object[]{"Airflow", null},
                new Object[]{"Tableau", null},
                new Object[]{"Power BI", null},
                new Object[]{"Databricks", null},
                new Object[]{"Snowflake", null},
                new Object[]{"BigQuery", null},
                new Object[]{"Redshift", null},
                new Object[]{"Looker", null},
                new Object[]{"RapidMiner", null},
                new Object[]{"Weka", null},
                new Object[]{"Caffe", null},
                new Object[]{"Theano", null},
                new Object[]{"MXNet", null},

                // Testing
                new Object[]{"JUnit", null},
                new Object[]{"TestNG", null},
                new Object[]{"Mockito", null},
                new Object[]{"Spock", null},
                new Object[]{"Jest", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/jest/jest-plain.svg"},
                new Object[]{"Mocha", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mocha/mocha-plain.svg"},
                new Object[]{"Chai", null},
                new Object[]{"Jasmine", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/jasmine/jasmine-plain.svg"},
                new Object[]{"Cypress", null},
                new Object[]{"Selenium", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/selenium/selenium-original.svg"},
                new Object[]{"Appium", null},
                new Object[]{"Playwright", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/playwright/playwright-original.svg"},
                new Object[]{"Puppeteer", null},
                new Object[]{"Postman", null},
                new Object[]{"Swagger", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/swagger/swagger-original.svg"},
                new Object[]{"JMeter", null},
                new Object[]{"K6", null},
                new Object[]{"Gatling", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/gatling/gatling-plain.svg"},
                new Object[]{"SonarQube", null},

                // Security
                new Object[]{"Kali Linux", null},
                new Object[]{"Burp Suite", null},
                new Object[]{"Metasploit", null},
                new Object[]{"Wireshark", null},
                new Object[]{"Nmap", null},
                new Object[]{"OWASP ZAP", null},
                new Object[]{"Snort", null},

                // Game Development
                new Object[]{"Unity", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/unity/unity-original.svg"},
                new Object[]{"Unreal Engine", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/unrealengine/unrealengine-original.svg"},
                new Object[]{"Godot", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/godot/godot-original.svg"},
                new Object[]{"CryEngine", null},
                new Object[]{"GameMaker Studio", null},
                new Object[]{"Cocos2d", null},
                new Object[]{"OpenGL", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/opengl/opengl-original.svg"},
                new Object[]{"DirectX", null},
                new Object[]{"Vulkan", null},
                new Object[]{"Metal", null},
                new Object[]{"WebGPU", null},
                new Object[]{"Blender", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/blender/blender-original.svg"},
                new Object[]{"Maya", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/maya/maya-original.svg"},
                new Object[]{"3ds Max", null},
                new Object[]{"ZBrush", null},
                new Object[]{"Substance Painter", null},

                // Blockchain
                new Object[]{"Bitcoin", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/bitcoin/bitcoin-original.svg"},
                new Object[]{"Ethereum", null},
                new Object[]{"Vyper", null},
                new Object[]{"Truffle", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/trufflesuite/trufflesuite-original.svg"},
                new Object[]{"Hardhat", null},
                new Object[]{"Ganache", null},
                new Object[]{"Web3.js", null},
                new Object[]{"Ethers.js", null},
                new Object[]{"IPFS", null},
                new Object[]{"Hyperledger Fabric", null},
                new Object[]{"Corda", null},
                new Object[]{"Polkadot", null},
                new Object[]{"Solana", null},
                new Object[]{"Cardano", null},
                new Object[]{"Chainlink", null},
                new Object[]{"MetaMask", null},

                // Version Control & Collaboration
                new Object[]{"Git", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/git/git-original.svg"},
                new Object[]{"GitHub", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/github/github-original.svg"},
                new Object[]{"GitLab", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/gitlab/gitlab-original.svg"},
                new Object[]{"Bitbucket", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/bitbucket/bitbucket-original.svg"},
                new Object[]{"SVN", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/subversion/subversion-original.svg"},
                new Object[]{"Mercurial", null},
                new Object[]{"Jira", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/jira/jira-original.svg"},
                new Object[]{"Confluence", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/confluence/confluence-original.svg"},
                new Object[]{"Trello", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/trello/trello-plain.svg"},
                new Object[]{"Asana", null},
                new Object[]{"Monday.com", null},
                new Object[]{"Notion", null},
                new Object[]{"Slack", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/slack/slack-original.svg"},
                new Object[]{"Discord", null},
                new Object[]{"Microsoft Teams", null},
                new Object[]{"Zoom", null},
                new Object[]{"Google Meet", null},

                // Design Tools
                new Object[]{"Figma", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/figma/figma-original.svg"},
                new Object[]{"Adobe XD", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/xd/xd-plain.svg"},
                new Object[]{"Sketch", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/sketch/sketch-original.svg"},
                new Object[]{"Photoshop", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/photoshop/photoshop-original.svg"},
                new Object[]{"Illustrator", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/illustrator/illustrator-plain.svg"},
                new Object[]{"Zeplin", null},
                new Object[]{"InVision", null},
                new Object[]{"Miro", null},

                // IDEs & Editors
                new Object[]{"VS Code", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/vscode/vscode-original.svg"},
                new Object[]{"IntelliJ IDEA", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/intellij/intellij-original.svg"},
                new Object[]{"Eclipse", null},
                new Object[]{"Android Studio", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/androidstudio/androidstudio-original.svg"},
                new Object[]{"Xcode", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/xcode/xcode-original.svg"},
                new Object[]{"PyCharm", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/pycharm/pycharm-original.svg"},
                new Object[]{"WebStorm", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/webstorm/webstorm-original.svg"},
                new Object[]{"Vim", "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/vim/vim-original.svg"},
                new Object[]{"Emacs", null},
                new Object[]{"Sublime Text", null},
                new Object[]{"Insomnia", null}
        );
    }
}
