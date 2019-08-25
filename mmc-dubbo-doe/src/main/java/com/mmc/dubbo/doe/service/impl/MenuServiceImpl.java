/*
 * Copyright (c) 2010-2020 Founder Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Founder. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements
 * you entered into with Founder.
 *
 */
package com.mmc.dubbo.doe.service.impl;

import com.mmc.dubbo.doe.auth.MenuNode;
import com.mmc.dubbo.doe.auth.MenuTree;
import com.mmc.dubbo.doe.service.MenuService;
import com.mmc.dubbo.doe.util.JsonFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Joey
 * @date 2018/11/26 16:21
 */
@Service("menuService")
@Slf4j
public class MenuServiceImpl implements MenuService {

    @Value("classpath:menu.json")
    private Resource resource;

    /**
     * cache urls.
     */
    private Map<Integer, String> cacheMap;

    @Override
    public String getUrl(Integer mid) {

        return cacheMap.get(mid);
    }

    private void cacheMenu(List<MenuNode> tree) {

        if (CollectionUtils.isEmpty(tree)) {
            return;
        }
        cacheMap = tree.stream().collect(Collectors.toMap(MenuNode::getMenuId, MenuTree::getMenuUrl));
    }

    /**
     * 真正生成文件方法.
     */
    @PostConstruct
    private void createFile() throws IOException {

        List<MenuNode> tree = JsonFileUtil.readList(resource.getInputStream(), MenuNode.class);
        MenuNode root = null;

        cacheMenu(tree);

        try {
            root = buildTree(tree, -1);
        } catch (Exception e) {
            log.error("fail to build the menu tree：", e);
            return;
        }
    }

    private MenuNode buildTree(List<MenuNode> menuList, int pMenuId) {
        MenuNode result = new MenuNode();
        MenuNode temp = new MenuNode();
        for (int i = 0; i < menuList.size(); i++) {
            if (menuList.get(i).getPmenuId() == pMenuId) {
                result.getChildren().add(menuList.get(i));
                temp = buildTree(menuList, menuList.get(i).getMenuId());
                if (temp.getChildren().size() > 0) {
                    menuList.get(i).setChildren(temp.getChildren());
                }
            }
        }
        return result;
    }

}
