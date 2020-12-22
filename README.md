# JFactory JPA DataRepository

# 安装

通过Gradle添加依赖
``` groovy
    implementation 'com.github.leeonky:jfactory-repo-jpa:0.1.0'
```

# 使用
```java
EntityManager entityManager;
JFactory jfactory = new JFactory(new JPADataRepository(entityManager));

// jfactory.create(xxx);
```

注意
- 考虑到关联表和外键问题，JPADataRepository::clear方法不会清除数据库数据，需要测试代码额外添加清理数据逻辑。