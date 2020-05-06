package com.qiwen.base.service.impl;

import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.config.db.QWPrivilegeTreeSorter;
import com.qiwen.base.entity.*;
import com.qiwen.base.repository.PrivilegeRepository;
import com.qiwen.base.service.IPrivilegeService;
import com.qiwen.base.util.PackageUtil;
import com.qiwen.base.util.StringUtil;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.qiwen.base.config.QWConstant.CACHE_MENU_KEY;
import static com.qiwen.base.config.QWConstant.CACHE_PRIVILEGE_KEY;
import static com.qiwen.base.util.ReflectUtil.getAllDeclaredMethod;

@Lazy
@Service
@CacheConfig(cacheNames = CACHE_PRIVILEGE_KEY)
public class PrivilegeServiceImpl implements IPrivilegeService {

    private static final Logger log = LoggerFactory.getLogger(PrivilegeServiceImpl.class);

    private final PrivilegeRepository privilegeRepo;

    private final QWPrivilegeTreeSorter privilegeTreeSorter;

    private final JPAQueryFactory factory;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public PrivilegeServiceImpl(PrivilegeRepository privilegeRepo, @Autowired(required = false) QWPrivilegeTreeSorter privilegeTreeSorter, JPAQueryFactory factory) {
        this.privilegeRepo = privilegeRepo;
        this.privilegeTreeSorter = privilegeTreeSorter;
        this.factory = factory;
    }

    @CacheEvict(allEntries = true)
    @Transactional
    @Override
    public void save(Privilege privilege) {
        privilegeRepo.save(privilege);
    }

    private JPAQuery<Privilege> joinQuery() {
        QUser user = QUser.user;
        QUserRole userRole = QUserRole.userRole;
        QRole role = QRole.role;
        QRolePrivilege rolePrivilege = QRolePrivilege.rolePrivilege;
        QPrivilege privilege = QPrivilege.privilege;

        return factory.selectFrom(privilege)
                .innerJoin(rolePrivilege)
                .on(privilege.name.eq(rolePrivilege.privilegeName))
                .innerJoin(role)
                .on(rolePrivilege.roleId.eq(role.id))
                .innerJoin(userRole)
                .on(role.id.eq(userRole.roleId))
                .innerJoin(user)
                .on(userRole.userId.eq(user.id));
    }

    @Cacheable(key = "'userName:' + #username")
    @Override
    public List<Privilege> findByUsername(String username) {
        QUser user = QUser.user;
        List<Privilege> privileges = joinQuery().where(user.username.eq(username)).fetch();
        return privileges;
    }

    @Cacheable(key = "'userId:' + #userId")
    @Override
    public List<Privilege> findByUserId(Long userId) {
        QUser user = QUser.user;
        List<Privilege> privileges = joinQuery().where(user.id.eq(userId)).fetch();
        return privileges;
    }

    @Cacheable(key = "'all'")
    @Override
    public List<Privilege> findAll() {
       return factory.selectFrom(QPrivilege.privilege).fetch();
    }

    @Cacheable(value = CACHE_PRIVILEGE_KEY, key = "'findByMenuId:' + #menuId")
    @Override
    public List<Privilege> findByMenuId(Long menuId) {
        QPrivilege qPrivilege = QPrivilege.privilege;
        QMenuPrivilege qMenuPrivilege = QMenuPrivilege.menuPrivilege;
        List<Privilege> privileges = factory.selectFrom(qPrivilege)
                .innerJoin(qMenuPrivilege)
                .on(qPrivilege.name.eq(qMenuPrivilege.privilegeName))
                .where(qMenuPrivilege.menuId.eq(menuId))
                .fetch();
        return privileges;
    }

    @Cacheable(key = "'findByRoleId:' + #roleId")
    @Override
    public List<Privilege> findByRoleId(Long roleId) {
        QPrivilege qPrivilege = QPrivilege.privilege;
        QRolePrivilege qRolePrivilege = QRolePrivilege.rolePrivilege;
        List<Privilege> privileges = factory.selectFrom(qPrivilege)
                .innerJoin(qRolePrivilege)
                .on(qPrivilege.name.eq(qRolePrivilege.privilegeName))
                .where(qRolePrivilege.roleId.eq(roleId))
                .fetch();
        return privileges;
    }

    @Cacheable(key = "'findNamesByRoleId:' + #roleId")
    @Override
    public List<String> findNamesByRoleId(Long roleId) {
        QPrivilege qPrivilege = QPrivilege.privilege;
        QRolePrivilege qRolePrivilege = QRolePrivilege.rolePrivilege;
        List<String> privileges = factory.select(qPrivilege.name)
                .from(qPrivilege)
                .innerJoin(qRolePrivilege)
                .on(qPrivilege.name.eq(qRolePrivilege.privilegeName))
                .where(qRolePrivilege.roleId.eq(roleId))
                .fetch();
        return privileges;
    }

    @Cacheable(key = "'findPrivilegeNamesByMenuId:' + #menuId")
    @Override
    public List<String> findPrivilegeNamesByMenuId(Long menuId) {
        QMenuPrivilege qMenuPrivilege = QMenuPrivilege.menuPrivilege;
        List<String> names = factory.select(qMenuPrivilege.privilegeName)
                .from(qMenuPrivilege)
                .where(qMenuPrivilege.menuId.eq(menuId))
                .fetch();
        return names;
    }

    @CacheEvict(cacheNames = {CACHE_MENU_KEY, CACHE_PRIVILEGE_KEY}, allEntries = true)
    @Transactional
    @Override
    public void generatePrivilegeInfo(String[] controllerBasePackages) throws ClassNotFoundException {
        Consumer<String> print = (content) -> {
            String t = StringUtils.repeat("*", 50);
            log.info(t + content + t);
        };

        List<String> classNameList = new ArrayList<>();
        Arrays.stream(controllerBasePackages).forEach(pkg -> {
            List<String> cns = PackageUtil.getClassName(pkg, true);
            classNameList.addAll(cns);
        });
        
        final Map<String, String> remarkMap = new LinkedHashMap<>();
        final Map<String, String> nameMap = new LinkedHashMap<>();
        final Map<String, String> urlMap = new LinkedHashMap<>();
        // 默认权限名称, 资源扫描中扫描到相关描述需要排除
        final List<String> defaultPrivilegeNames = new ArrayList<>();
        final String deliRegex = "\\s*\\>\\s*";
        final  String separator = ">";

        Consumer<String[]> addDefaultPermissionResource = (final String[] resources) -> {
            nameMap.put(resources[0], resources[1]);
            remarkMap.put(resources[0], resources[2]);
            urlMap.put(resources[0], resources[3]);
            defaultPrivilegeNames.add(resources[1]);
        };

        // 添加默认权限节点
        addDefaultPermissionResource.accept(new String[]{ "local.key", "qw-local", "本地环境权限", "" });
        addDefaultPermissionResource.accept(new String[]{ "sit.key", "qw-sit", "测试环境访问权限", "" });
        addDefaultPermissionResource.accept(new String[]{ "uat.key", "qw-uat", "用户测试环境访问权限", "" });
        addDefaultPermissionResource.accept(new String[]{ "pre.key", "qw-pre", "预览环境访问权限", "" });
        addDefaultPermissionResource.accept(new String[]{ "prd.key", "qw-prd", "生产环境访问权限", "" });

        // 扫描出所有 Controller 描述的权限信息
        print.accept("开始扫描资源");
        for (String className : classNameList) {
            log.info("class name: " + className);
            Class targetClazz = Class.forName(className);
            Desc classDesc = (Desc) targetClazz.getAnnotation(Desc.class);

            // 不需要拦截
            if(classDesc == null || !classDesc.requiredPermission()) {
                log.info("{} 下方法不需要生成资源标识", targetClazz.getName());
                continue;
            }

            Controller classCon = (Controller) targetClazz.getAnnotation(Controller.class);
            RestController classRecon = (RestController) targetClazz.getAnnotation(RestController.class);
            RequestMapping classRm = (RequestMapping) targetClazz.getAnnotation(RequestMapping.class);

            // @Controller & @RestController 内中出现一个，
            // @RequestMapping 必须出现
            if(!((classCon != null ^ classRecon != null) && classRm != null)) {
                continue;
            }

            // 读取类上 @Desc 元数据
            String baseRemark = StringUtil.getFirstNotEmptyStr(classDesc.groupDesc(), classDesc.value(), "not-group-desc");;
            String baseName = StringUtil.getFirstNotEmptyStr(classDesc.groupName(), "not-group-name");
            String baseUrl = StringUtil.getFirstNotEmptyStr(classRm.value());

            // 生成 URL 映射路径，保证 baseUrl 开始为 /, 结束不为 /
            baseUrl = !baseUrl.startsWith("/") ? "/" + baseUrl : baseUrl;
            baseUrl = !baseUrl.endsWith("/") ? baseUrl : baseUrl.substring(0, baseUrl.length() - 1);

            // 查找方法注释信息
            List<Method> methodList = getAllDeclaredMethod(targetClazz);
            Desc methodDesc;
            String methodUrl;

            for (Method method : methodList) {
                methodDesc = method.getAnnotation(Desc.class);
                methodUrl = getUrlByMethod(method);

                if(methodUrl == null) {
                    continue;
                }

                if(methodDesc == null || StringUtils.isEmpty(methodDesc.name()) || !methodDesc.requiredPermission()) {
                    log.info("{}.{} 方法不需要生成资源标识", className, method.getName());
                    continue;
                }

                String key = className + "." + method.getName();
                String methodDescName = relativePathToAbsolutePath(baseName + separator + methodDesc.name(), deliRegex, separator);

                // 该 name 已经包含, 跳过
                if(defaultPrivilegeNames.contains(methodDescName)) {
                    continue;
                }

                nameMap.put(key, methodDescName);
                // 添加资源描述 url
                String methodDescRemark = relativePathToAbsolutePath(baseRemark + separator + methodDesc.value(), deliRegex, separator);
                remarkMap.put(key, methodDescRemark);
                // 添加 url
                methodUrl = methodUrl.startsWith("/") ? methodUrl : "/" + methodUrl;
                String url = baseUrl + methodUrl;
                url = url.replaceAll("\\{\\w+\\}", "*");
                urlMap.put(key, url);
            }
        }
        print.accept("扫描完成");

        log.info("\r\n\r\n");
        print.accept("生成权限表信息-start");
        List<Privilege> privileges = convertToSimplePrivileges(urlMap, nameMap, remarkMap);
        privileges.forEach(privilege -> log.info(privilege.toString()));
        print.accept("生成权限表信息-end");

        recursionPrivileges(privileges, "1");
        factory.delete(QPrivilege.privilege).execute();
        privilegeRepo.saveAll(privileges);
    }

    /**
     * 根据指定的路径分隔符将相对路径转换为绝对路径
     * @param path
     * @param deliRegex
     * @return
     */
    private String relativePathToAbsolutePath(String path, String deliRegex, String separator) {
        String[] patts = path.split(deliRegex);
        LinkedList<String> absPatts = new LinkedList<>();

        for(int i = 0; patts != null && i < patts.length; i++) {
            String patt = patts[i];

            if("..".equals(patt)) {
                if(absPatts.size() > 0) {
                    absPatts.removeLast();
                }
            } else if(!".".equals(patt)) {
                absPatts.add(patt);
            }
        }
        return StringUtils.join(absPatts, separator);
    }

    /**
     * 根据 parentId(父节点Id) 和 name(节点名称) 查询兄弟节点.
     * @param privileges
     * @param parentId
     * @param name
     * @return
     */
    private Map<String, Object> generatePrivilegeTreeInfo(List<Privilege> privileges, String parentId, String name) {
        List<Privilege> brothers = privileges.stream()
                .filter(privilege -> parentId.equals(privilege.getTreeParentId()))
                .collect(Collectors.toList());

        List<Privilege> matches = brothers.stream()
                .filter(privilege -> name.equals(privilege.getName()))
                .collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();

        // 如果父节点下面没有同名的节点
        if (CollectionUtils.isEmpty(matches)) {
            map.put("exist", false);
            map.put("treeId", parentId + "-" + brothers.size());
            map.put("orderIndex", brothers.size());
            return map;
        }

        map.put("exist", true);
        map.put("privilege", matches.get(0));
        return map;
    }

    /**
     * 转换出权限树<br/>
     * </code>
     * @param urlMap
     * @param nameMap
     * @param remarkMap
     */
    private List<Privilege> convertToSimplePrivileges(Map<String, String> urlMap, Map<String, String> nameMap, Map<String, String> remarkMap) {
        List<Privilege> privileges = new ArrayList<>();
        String splitRegex = "\\s*>\\s*";
        Map<String, List<String>> nameEnforceMap = new LinkedHashMap<>();
        Map<String, List<String>> remarkEnforceMap = new LinkedHashMap<>();

        /**
         * 将 nameMap 和 remarkMap 转换为如下形式:
         *  foo.class1 -> [user-admin, user-add, ...]
         *  foo.class2 -> [role-admin, role-add, ...]
         */
        for(Iterator<Map.Entry<String, String>> it = nameMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> next = it.next();
            String key = next.getKey();
            String namesStr = next.getValue();
            String remarkStr = remarkMap.get(key);
            List<String> names = Arrays.asList(namesStr.split(splitRegex));
            List<String> remarks = Arrays.asList(remarkStr.split(splitRegex));
            nameEnforceMap.put(key, names);
            remarkEnforceMap.put(key, remarks);
        }

        // 生成权限树
        for(Iterator<Map.Entry<String, List<String>>> it = nameEnforceMap.entrySet().iterator(); it.hasNext();) {
            String treeParentId = "1";
            Map.Entry<String, List<String>> next = it.next();
            List<String> names = next.getValue();
            List<String> remarks = remarkEnforceMap.get(next.getKey());
            // 权限名称集合
            int namesLen = names.size();
            // 权限注释集合
            int remarksLen = remarks.size();

            for(int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                // 根节点默认为 1, 查询兄弟节点。
                // 1. 如果能够查询得到, 则返回的 MAP 格式为: {exist: true,  privilege: {...}(兄弟节点信息)}
                // 2. 如果不能够查找到, 则返回的 MAP 格式如下: {exist: false, treeId: ...(新生成的 TreeID, 即以该 name 生成新子节点), orderIndex: ...(在兄弟节点中的顺序)}
                Map<String, Object> map = generatePrivilegeTreeInfo(privileges, treeParentId, name);

                // 生成一个新节点。
                if(!(Boolean)map.get("exist")) {
                    Privilege privilege = new Privilege();
                    privilege.setName(name);
                    privilege.setTreeId((String) map.get("treeId"));
                    privilege.setTreeParentId(treeParentId);
                    privilege.setOrderIndex((Integer) map.get("orderIndex"));
                    privilege.setStatus(1);
                    privilege.setUrls(i == namesLen - 1 ? urlMap.get(next.getKey()) : "");
                    privilege.setDescription(i < remarksLen ? remarks.get(i) : "");
                    privileges.add(privilege);
                    treeParentId = (String) map.get("treeId");
                } else {
                    Privilege oldPrivilege = (Privilege) map.get("privilege");
                    String newRemark = i < remarksLen ? remarks.get(i) : "";
                    // 查询描述已经生成的兄弟节点的描述信息是否包含本条权限的描述信息。
                    boolean exist = Arrays.asList(
                            oldPrivilege.getDescription().split("\\s*\\,\\s*")
                        ).contains(newRemark);
                    // 不包含，则直接将本条权限权限的描述信息附加到已经存在的兄弟节点后面。
                    if(!exist) {
                        oldPrivilege.setDescription(oldPrivilege.getDescription() + "," + (i < remarksLen ? remarks.get(i) : ""));
                    }

                    // 将本条权限表示的 url 附加到已经存在的兄弟节点 url 后面。
                    if(i == namesLen - 1) {
                        String oldUrls = oldPrivilege.getUrls();
                        String url = urlMap.get(next.getKey());
                        oldUrls += "," + url;
                        oldPrivilege.setUrls(oldUrls);
                    }

                    treeParentId = oldPrivilege.getTreeId();
                }
            }
        }
        return privileges;
    }

    private String getUrlByMethod(Method method) {
        RequestMapping rm = method.getAnnotation(RequestMapping.class);
        String[] tArr = null;
        if(rm != null) {
            tArr = rm.value();
        }
        GetMapping gm = method.getAnnotation(GetMapping.class);
        if(gm != null) {
            tArr = gm.value();
        }
        PostMapping pm = method.getAnnotation(PostMapping.class);
        if(pm != null) {
            tArr = pm.value();
        }
        DeleteMapping dm = method.getAnnotation(DeleteMapping.class);
        if(dm != null) {
            tArr = dm.value();
        }
        PutMapping putm = method.getAnnotation(PutMapping.class);
        if(putm != null) {
            tArr = putm.value();
        }

        if(tArr != null) {
            return tArr.length > 0 ? tArr[0] : "";
        }

        return null;
    }

    private List<Privilege> findChildren(List<Privilege> ps, String treeId) {
        return ps.stream().filter(tp -> tp.getTreeParentId().equals(treeId)).collect(Collectors.toList());
    }

    private Privilege findObjByTreeId(List<Privilege> ps, String treeId) {
        Optional<Privilege> firstOp = ps.stream().filter(tp -> tp.getTreeId().equals(treeId)).findFirst();
        if(firstOp.isPresent()) {
            return firstOp.get();
        }
        return null;
    };

    private List<Privilege> findParents(List<Privilege> ps, String treeId) {
        List<Privilege> parents = new ArrayList<>();
        Privilege tp = findObjByTreeId(ps, treeId);

        if(tp == null) {
           return parents;
        }

        String treeParentId = tp.getTreeParentId();

        while (tp != null) {
            tp = findObjByTreeId(ps, treeParentId);
            if(tp == null) {
                break;
            }
            parents.add(0, tp);
            treeParentId = tp.getTreeParentId();
        }

        return parents;
    }

    private List<Privilege> recursionPrivileges(List<Privilege> ps, String treeId) {
        Privilege realObj = findObjByTreeId(ps, treeId);
        if(realObj == null) {
            realObj = new Privilege();
            realObj.setName("root");
            realObj.setTreeId(treeId);
            realObj.setOrderIndex(0);
        }
        List<Privilege> children = findChildren(ps, treeId);
        List<Privilege> tParents = findParents(ps, treeId);
        if(privilegeTreeSorter != null) {
            privilegeTreeSorter.sort(realObj, tParents, children, ps);
        }
        children.forEach(child -> {
            recursionPrivileges(ps, child.getTreeId());
        });
        return null;
    }

}
