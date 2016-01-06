package utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import models.admin.AdminUser;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.cache.Cache;

/**
 * Created by ShenTeng on 2015/1/14.
 */
public class UserLoginCache {

    public static AdminUser getByTokenAndRefresh(String token) {
        if (null == token) {
            return null;
        }
        if (StringUtils.isBlank(token)) {
            return null;
        }

        AdminUser user = (AdminUser) Cache.get(Constants.NAMESPACE_SESSION_USER + token);
        if (null == user) {
            return null;
        }

        List<String> tokenList = (List<String>) Cache.get(Constants.NAMESPACE_SESSION_TOKEN + user.getId());

        if (null == tokenList || !tokenList.contains(token)) {
            Cache.remove(Constants.NAMESPACE_SESSION_USER + token);
            return null;
        }

        Cache.set(Constants.NAMESPACE_SESSION_USER + token, user, Constants.SESSION_TOKEN_EXPIRE_IN);
        Cache.set(Constants.NAMESPACE_SESSION_TOKEN + user.getId(), tokenList, Constants.SESSION_TOKEN_EXPIRE_IN + 30);

        return user;
    }

    public static AdminUser getByToken(String token) {
        if (null == token) {
            return null;
        }
        if (StringUtils.isBlank(token)) {
            return null;
        }

        AdminUser user = (AdminUser) Cache.get(Constants.NAMESPACE_SESSION_USER + token);
        if (null == user) {
            return null;
        }

        List<String> tokenList = (List<String>) Cache.get(Constants.NAMESPACE_SESSION_TOKEN + user.getId());

        if (null == tokenList || !tokenList.contains(token)) {
            return null;
        }

        return user;
    }

    public static boolean isLoginByToken(String token) {
        return null != getByToken(token);
    }

    public static String save(AdminUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");

        Cache.set(Constants.NAMESPACE_SESSION_USER + token, user, Constants.SESSION_TOKEN_EXPIRE_IN);


        List<String> tokenList = (List<String>) Cache.get(Constants.NAMESPACE_SESSION_TOKEN + user.getId());

        if (null == tokenList) {
            tokenList = new ArrayList<>();
        }

        if (!tokenList.contains(token)) {
            tokenList.add(token);
        }

        Cache.set(Constants.NAMESPACE_SESSION_TOKEN + user.getId(), tokenList, Constants.SESSION_TOKEN_EXPIRE_IN + 30);


        return token;
    }

    public static void update(AdminUser user, boolean isCopyTransientValue) {
        if (null != user && null != user.getId()) {
            List<String> tokenList = (List<String>) Cache.get(Constants.NAMESPACE_SESSION_TOKEN + user.getId());
            if (CollectionUtils.isNotEmpty(tokenList)) {
                Iterator<String> iterator = tokenList.iterator();

                while (iterator.hasNext()) {
                    String token = iterator.next();

                    AdminUser cacheUser = (AdminUser) Cache.get(Constants.NAMESPACE_SESSION_USER + token);
                    if (null == cacheUser) {
                        iterator.remove();
                    } else {
                        Cache.set(Constants.NAMESPACE_SESSION_USER + token, user, Constants.SESSION_TOKEN_EXPIRE_IN);
                    }
                }

                Cache.set(Constants.NAMESPACE_SESSION_TOKEN + user.getId(), tokenList, Constants.SESSION_TOKEN_EXPIRE_IN + 30);
            }
        }
    }

    public static void removeByToken(String token) {
        if (StringUtils.isNotBlank(token)) {
            AdminUser user = (AdminUser) Cache.get(Constants.NAMESPACE_SESSION_USER + token);
            Cache.remove(Constants.NAMESPACE_SESSION_USER + token);

            if (null != user) {
                List<String> tokenList = (List<String>) Cache.get(Constants.NAMESPACE_SESSION_TOKEN + user.getId());

                if (null != tokenList && tokenList.contains(token)) {
                    tokenList.remove(token);
                    Cache.set(Constants.NAMESPACE_SESSION_TOKEN + user.getId(), tokenList, Constants.SESSION_TOKEN_EXPIRE_IN + 30);
                }
            }
        }
    }

    public static void kickById(Long userId) {
        List<String> tokenList = (List<String>) Cache.get(Constants.NAMESPACE_SESSION_TOKEN + userId);
        Cache.remove(Constants.NAMESPACE_SESSION_TOKEN + userId);
        if (CollectionUtils.isNotEmpty(tokenList)) {
            for (String token : tokenList) {
                Cache.remove(Constants.NAMESPACE_SESSION_USER + token);
            }
        }
    }

}
