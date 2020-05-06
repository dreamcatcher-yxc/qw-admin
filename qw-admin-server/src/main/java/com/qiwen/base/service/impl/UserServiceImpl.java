package com.qiwen.base.service.impl;

import com.google.common.io.Files;
import com.qiwen.base.entity.*;
import com.qiwen.base.exception.BusinessException;
import com.qiwen.base.repository.UserRepository;
import com.qiwen.base.repository.UserRoleRepository;
import com.qiwen.base.service.IFileMapService;
import com.qiwen.base.service.IUserService;
import com.qiwen.base.util.NameImgUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.qiwen.base.config.QWConstant.*;
import static com.qiwen.base.util.PasswordUtil.encryptPassword;
import static com.qiwen.base.util.QueryDSLUtil.page;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    private final JPAQueryFactory factory;

    private final UserRepository userRepo;

    private final UserRoleRepository userRoleRepo;

    private final IFileMapService fileMapService;

    public UserServiceImpl(JPAQueryFactory factory, UserRepository userRepo, UserRoleRepository userRoleRepo, IFileMapService fileMapService) {
        this.factory = factory;
        this.userRepo = userRepo;
        this.userRoleRepo = userRoleRepo;
        this.fileMapService = fileMapService;
    }

    private String generateNicknameHeader(String nickname) throws IOException {
        Path tempPath = fileMapService.generateNewTempFile("jpg");
        NameImgUtil.generateImg(nickname, tempPath.getParent().toAbsolutePath().toString(), Files.getNameWithoutExtension(tempPath.toFile().getName()));
        FileMap fm = fileMapService.save(null, tempPath);
        return fm.getFileId();
    }

    @Override
    public User findById(long id) {
        QUser user = QUser.user;
        return factory.selectFrom(user)
                        .where(user.id.eq(id))
                        .fetchFirst();
    }

    @Override
    public User findByUsernameAndPassword(String username, String password) {
        QUser user = QUser.user;
        User result = factory.selectFrom(user)
                .where(
                        user.username.eq(username).and(
                                user.password.eq(password)
                        )
                ).fetchFirst();
        return result;
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        QUser user = QUser.user;
        return page(factory.selectFrom(user), pageable);
    }

    @Override
    public Page<User> findAll(Pageable pageable,  Long... excludeIds) {
        QUser user = QUser.user;
        return page(factory.selectFrom(user).where(user.id.notIn(excludeIds)), pageable);
    }

    @Override
    public Page<User> findByCondition(String username, Pageable pageable) {
        Page<User> page = userRepo.findAll((r, q, b) -> {
            List<Predicate> pres = new ArrayList<>();

            if (!StringUtils.isEmpty(username)) {
                pres.add(b.like(r.get("username"), "%" + username + "%"));
            }
            return q.where(pres.toArray(new Predicate[pres.size()]))
                    .orderBy(b.desc(r.get("createDate")))
                    .getRestriction();
        }, pageable);

        // 过滤 password
        if(page.getTotalElements() > 0) {
            page.getContent().forEach(user -> user.setPassword(""));
        }

        return page;
    }

    @Cacheable(value = CACHE_USER_KEY, key = "'username:' + #username")
    @Override
    public User findByUsername(String username) {
        QUser qUser = QUser.user;
        return factory.selectFrom(qUser).where(qUser.username.eq(username)).fetchOne();
    }

    @Transactional
    @Override
    public void save(User user) {
        user.setPassword(encryptPassword(user.getPassword()));
        // 昵称存在, 根据昵称设置头像.
        String nickname = user.getNickname();

        if(!StringUtils.isEmpty(nickname) && StringUtils.isEmpty(user.getHeader())) {
            try {
                user.setHeader(generateNicknameHeader(nickname));
            } catch (IOException e) {
                throw new BusinessException("生成昵称头像失败");
            }
        }

        userRepo.save(user);
    }

    @Transactional
    @Override
    public void save(List<User> users) {
        users.forEach(user -> {
            user.setPassword(encryptPassword(user.getPassword()));
            // 昵称存在, 根据昵称设置头像.
            String nickname = user.getNickname();

            if(!StringUtils.isEmpty(nickname) && StringUtils.isEmpty(user.getHeader())) {
                try {
                    user.setHeader(generateNicknameHeader(nickname));
                } catch (IOException e) {
                    throw new BusinessException("生成昵称头像失败");
                }
            }
        });

        userRepo.saveAll(users);
    }

    @CacheEvict(value = CACHE_USER_KEY, allEntries = true)
    @Transactional
    @Override
    public void deleteByIds(Long... ids) {
        QUser qUser = QUser.user;
        QUserRole qUserRole = QUserRole.userRole;

        List<String> headers = factory.select(qUser.header)
                .from(qUser)
                .where(qUser.id.in(ids))
                .fetch();

        long effectRow = factory.delete(qUser)
                .where(qUser.id.in(ids))
                .execute();
        if(effectRow > 0) {
            // 删除和该用户相关联的记录
            factory.delete(qUserRole)
                    .where(qUserRole.userId.in(ids))
                    .execute();
            try {
                fileMapService.deleteByFileIds(headers.toArray(new String[headers.size()]));
            } catch (Exception e) {
                log.info("删除用户头像图片失败, cause: {}, message: {}", e.getCause(), e.getMessage());
            }
        }
    }

    @CacheEvict(value = CACHE_USER_KEY, allEntries = true)
    @Transactional
    @Override
    public void modifyById(User user, boolean autoReplaceHeader) {
        QUser qUser = QUser.user;
        User realUser = factory.selectFrom(qUser)
                .where(qUser.id.eq(user.getId()))
                .fetchFirst();
        realUser.setPhone(user.getPhone());
        realUser.setEmail(user.getEmail());
        realUser.setGender(user.getGender());
        realUser.setNickname(user.getNickname());
        realUser.setStatus(user.getStatus());

        String nickname = user.getNickname();
        String needDeleteImgId =  null;
        if(autoReplaceHeader) {
            try {
                needDeleteImgId = realUser.getHeader();
                realUser.setHeader(generateNicknameHeader(nickname));
            } catch (IOException e) {
                throw new BusinessException("生成昵称头像失败");
            }
        }

        userRepo.save(realUser);

        if(needDeleteImgId != null) {
            fileMapService.deleteByFileIds(needDeleteImgId);
        }
    }

    @CacheEvict(cacheNames = {CACHE_MENU_KEY, CACHE_PRIVILEGE_KEY}, allEntries = true)
    @Transactional
    @Override
    public void modifyUserRoles(Long userId, List<UserRole> userRoles) {
        QUserRole qUserRole = QUserRole.userRole;
        factory.delete(qUserRole)
                .where(qUserRole.userId.eq(userId))
                .execute();
        userRoleRepo.saveAll(userRoles);
    }

    @CacheEvict(value = CACHE_USER_KEY, allEntries = true)
    @Transactional
    @Override
    public void modifyPersonalInfoById(User user, boolean onlyChangeHeader) {
        QUser qUser = QUser.user;
        User realUser = factory.selectFrom(qUser)
                .where(qUser.id.eq(user.getId()))
                .fetchFirst();

        String header = user.getHeader();
        String needDeleteImgId =  null;

        if(fileMapService.isTempFile(header)) {
            Path tempFilePath = fileMapService.findTempFile(header);
            FileMap fm = fileMapService.save(null, tempFilePath);
            needDeleteImgId = realUser.getHeader();
            realUser.setHeader(fm.getFileId());
        }

        if(!onlyChangeHeader) {
            realUser.setPhone(user.getPhone());
            realUser.setEmail(user.getEmail());
            realUser.setGender(user.getGender());
            realUser.setNickname(user.getNickname());
        }

        userRepo.save(realUser);

        if(needDeleteImgId != null) {
            fileMapService.deleteByFileIds(needDeleteImgId);
        }
    }

    @CacheEvict(value = CACHE_USER_KEY, allEntries = true)
    @Transactional
    @Override
    public void modifyPassword(Long userId, String srcPassword, String newPassword) {
        Optional<User> userOpt = userRepo.findOne((r, q, b) -> {
            List<Predicate> pres = new ArrayList<>();
            pres.add(b.equal(r.get("id"), userId));
            return q.where(pres.toArray(new Predicate[pres.size()]))
                    .orderBy(b.desc(r.get("createDate")))
                    .getRestriction();
        });

        if(!userOpt.isPresent()) {
            throw new BusinessException("系统异常");
        }

        User realUser = userOpt.get();
        String realEncryptionPassword = realUser.getPassword();
        String encryptionPassword = encryptPassword(srcPassword);
        String newEncryptionPassword = encryptPassword(newPassword);

        if(!realEncryptionPassword.equals(encryptionPassword)) {
            throw new BusinessException("原密码错误");
        }

        if(!realEncryptionPassword.equals(newEncryptionPassword)) {
            realUser.setPassword(newEncryptionPassword);
            userRepo.save(realUser);
        }
    }
}
