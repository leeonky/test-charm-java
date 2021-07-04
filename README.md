# JFactory JPA DataRepository

[![travis-ci](https://travis-ci.com/leeonky/jfactory-repo-jpa.svg?branch=master)](https://travis-ci.com/github/leeonky/jfactory-repo-jpa)
[![coveralls](https://img.shields.io/coveralls/github/leeonky/jfactory-repo-jpa/master.svg)](https://coveralls.io/github/leeonky/jfactory-repo-jpa)
[![Lost commit](https://img.shields.io/github/last-commit/leeonky/jfactory-repo-jpa.svg)](https://github.com/leeonky/jfactory-repo-jpa)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.leeonky/jfactory-repo-jpa.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.leeonky/jfactory-repo-jpa)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/6fd6832505594ed09070add129b570a6)](https://www.codacy.com/gh/leeonky/jfactory-repo-jpa/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=leeonky/jfactory-repo-jpa&amp;utm_campaign=Badge_Grade)
[![Maintainability](https://api.codeclimate.com/v1/badges/62a8a3826b05eefd1f3b/maintainability)](https://codeclimate.com/github/leeonky/jfactory-repo-jpa/maintainability)
[![Code Climate issues](https://img.shields.io/codeclimate/issues/leeonky/jfactory-repo-jpa.svg)](https://codeclimate.com/github/leeonky/jfactory-repo-jpa/maintainability)
[![Code Climate maintainability (percentage)](https://img.shields.io/codeclimate/maintainability-percentage/leeonky/jfactory-repo-jpa.svg)](https://codeclimate.com/github/leeonky/jfactory-repo-jpa/maintainability)


# 安装

通过Gradle添加依赖
``` groovy
    implementation 'com.github.leeonky:jfactory-repo-jpa:0.1.0'
```

# 使用
```java
EntityManagerFactory entityManagerFactory
JFactory jfactory = new JFactory(new JPADataRepository(entityManagerFactory));

// jfactory.create(xxx);
```

注意
- 考虑到关联表和外键问题，JPADataRepository::clear方法仅调用`EntityManager::clear()`清除缓存，不会清除数据库数据，需要测试代码额外添加清理数据逻辑。
