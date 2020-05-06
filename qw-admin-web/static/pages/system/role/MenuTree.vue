<template>
  <span>
    <a-spin :spinning="isLoading">
      <div id="menu-tree" class="ztree"></div>
    </a-spin>
    <template v-if="isEdit">
      <menu-edit :show.sync="isEdit" :data="editData" @on-close="reloadMenuNode"></menu-edit>
    </template>
  </span>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'mixins',
  'zTree',
  'jquery',
  'alias!@API/system/menu',
  'alias!@PAGE/system/role/trees',
  'css!../../assets/libs/ztree/zTreeStyle/zTreeStyle.css'
], function(MXS, _zTree, $, MenuAPIS, trees, __cssText, resolve, reject) {
  resolve({
    name: 'MenuTree',
    mixins: [MXS.CommonMixin, MXS.AuthMixin],
    components: {
      MenuEdit: load('./MenuEdit')
    },
    data () {
      return {
        isEdit: false,
        editData: {},
        checkedNodeIds: []
      }
    },
    mounted () {
      // 初始化权限信息
      this.$$fetchAuth([
        'menu-list',
        'menu-add',
        'menu-move',
        'menu-edit',
        'menu-delete',
        'menu-add-menu-privilege',
        'menu-find-privilege'
      ]).then(() => this.init())
    },
    methods: {
      fetchMenuData () {
        return MenuAPIS.queryMenus()
          .then(resp => {
            let menus = resp.sdata.menus||[];
            return menus;
          });
      },

      initTree (menus, checkedNodeIds) {
        var self = this;
        var checkedNodeIds

        var treeNodes = trees.convert(!!menus ? menus : [], function(i, n) {
          return {
            bId : n.id,
            id : n.treeId,
            pId : n.treeParentId,
            orderIndex : n.orderIndex,
            open: checkedNodeIds.length === 0 ? !n.isLeaf : $.inArray(n.treeId, checkedNodeIds) >= 0,
            isLeaf : n.isLeaf,
            name : n.name,
            url : n.url,
            description : n.description
          };
        });

        var zTree = undefined;
        var MAX_LEVEL = 2; // 最大允许创建的节点的深度
        var MAXIMUM_NUMBER_OF_THE_SAME_LEVEL = 100; // 同级最多允许创建 100 个节点

        function getSubAllNodes(treeNode, notTop) {
          var arr = [];
          if (!!notTop) {
            arr.push(treeNode)
          }
          if (treeNode.isParent) {
            for (let i = 0; i < treeNode.children.length; i++) {
                arr = arr.concat(getSubAllNodes(treeNode.children[i], true));
            }
          }
            return arr;
        }

        function getSubAllNodesLength(treeNode, notTop) {
          var count = 0;
          if (!!notTop) {
              count++;
          }
          if (treeNode.isParent) {
            for (let i = 0; i < treeNode.children.length; i++) {
                count += getSubAllNodesLength(treeNode.children[i], true);
            }
          }
          return count;
        }

        /**
         * 重新生成所有的树节点的 id 和 pId 信息.
         * @param excludeSubTreeRootId: 不包含的子树的根节点.
         * @returns {Array}
         */
        function generateTreeNodeInfo(excludeSubTreeRootId) {
          var root = zTree.getNodesByFilter(function (node) { return node.level == 0 }, true);
          function generate(parentId, parentNode, newNodes) {
            if(parentNode.isParent) {
                var baseIndex = 0;
                for(var i = 0; i < parentNode.children.length; i++) {
                    var currNode = parentNode.children[i];
                    if(excludeSubTreeRootId === currNode.id) {
                        continue;
                    }
                    var newId = parentId + '-' + baseIndex;
                    newNodes.push({
                        id : newId,
                        pId : parentId,
                        orderIndex : baseIndex,
                        isLeaf :  !currNode.isParent || (currNode.isParent && currNode.children.length === 1 && currNode.children[0].id === excludeSubTreeRootId) ? 1 : 0,
                        oldId : currNode.id,
                        oldPId : currNode.pId,
                        oldOrderIndex : currNode.orderIndex,
                        oldIsLeaf : currNode.isLeaf,
                        bId : currNode.bId,
                        open: currNode.open
                    });
                    baseIndex++;
                    generate(newId, currNode, newNodes);
                }
            }
          }

          var newNodes = [];
          generate(root.id, root, newNodes);
          return newNodes;
        }

        function getAllNodesLength() {
            var root = zTree.getNodesByFilter(function (node) { return node.level == 0 }, true);
            return getSubAllNodesLength(root) + 1;
        }

        function beforeEditName(treeId, treeNode) {
          zTree.selectNode(treeNode);
          self.isEdit = true;
          self.editData = { menuId: treeNode.bId, treeNode: treeNode };
          return false;
        }

        function beforeRename(treeId, treeNode, newName, isCancel) {
          if (newName.trim().length == 0) {
              self.$message.error('节点名称不能为空');
              setTimeout(function () {
                zTree.editName(treeNode)
              }, 10);
              return false;
          }

          var formData = {
            id  : treeNode.bId,
            name : newName
          };
          MenuAPIS.editMenuName(formData)
            .then(resp => {
              // ...
            })
            .catch(resp => {
              self.$message.error(resp.message);
              zTree.editName(treeNode);
            });
          return true;
        }

        function beforeRemove(treeId, treeNode) {
          zTree.selectNode(treeNode);
          var msg = '';
          if(treeNode.isParent) {
              msg = '【' + treeNode.name + '】节点存在【' + treeNode.children.length + '】 个子节点, 确认删除吗?';
          } else {
              msg = '确认删除【' + treeNode.name + '】节点吗?';
          }
          self.$confirm({
            title: '删除确认',
            content: msg,
            okText: '确认',
            cancelText: '取消',
            onOk: () => {
              self.showLoading();
              var needInfos = generateTreeNodeInfo(treeNode.id);
              var needRefershInfos = [];
              for(var i = 0; i < needInfos.length; i++) {
                  var tInfo = needInfos[i];
                  if(tInfo.id !== tInfo.oldId
                      || tInfo.pId !== tInfo.oldPId
                      || tInfo.isLeaf !== tInfo.oldIsLeaf
                      || tInfo.orderIndex !== tInfo.oldOrderIndex) {
                      needRefershInfos.push(tInfo);
                  }
              }
              var adjusts = $.map(needRefershInfos, function (n, i) {
                  return n.bId + ',' + n.id + ',' + n.pId + ',' + n.isLeaf + ',' + n.orderIndex;
              }).join(';');

              var ids = $.map(getSubAllNodes(treeNode, true), function(n, i) {
                return n.bId;
              }).join(',');

              var formData = {ids : ids, adjusts : adjusts};
              self.showLoading();
              MenuAPIS.deleteMenuNodes(formData)
                .then(resp => {
                  zTree.removeNode(treeNode);
                  self.$message.success('删除成功');
                })
                .catch(resp => {
                  self.$message.error(resp.message);
                })
                .finally(() => self.hideLoading())
                .then(() => {
                  self.checkedNodeIds = needInfos.filter(node => node.open).map(node => node.id);
                  self.init();
                });
            },
            onCancel() {}
          });
          return false;
        }

        function onRemove(e, treeId, treeNode) {
        }

        function onRename(e, treeId, treeNode, isCancel) {
        }

        function showRemoveBtn(treeId, treeNode) {
          return self.$$hasAuth('menu-delete') && !!treeNode.pId;
        }

        function showRenameBtn(treeId, treeNode) {
          return self.$$hasAuth('menu-edit') && !!treeNode.pId;
        }

        // 动态添加添加按钮
        function addHoverDom(treeId, treeNode) {
          // rename -> 编辑, remove -> 删除
          // begin
          var $renameDom = $('#' + treeNode.tId + '_edit');
          if($renameDom.length > 0) {
              $renameDom.attr('title', '编辑');
              $renameDom.hover(() => $renameDom.css('border', '1px solid red'), () => $renameDom.css('border', 'initial'));
          }

          var $removeDom = $('#' + treeNode.tId + '_remove');
          if($removeDom.length > 0) {
              $removeDom.attr('title', '删除');
              $removeDom.hover(() => $removeDom.css('border', '1px solid red'), () => $removeDom.css('border', 'initial'));
          }
          // end

          var $sObj = $("#" + treeNode.tId + "_span");

          if (treeNode.editNameFlag || $("#addBtn_" + treeNode.tId).length > 0) {
            return;
          };

          if (treeNode.level >= MAX_LEVEL) {
              return;
          }
          var base = treeNode.isParent ? treeNode.children.length : 0;
          if (base >= MAXIMUM_NUMBER_OF_THE_SAME_LEVEL) {
              return;
          }

          if(self.$$hasAuth('menu-add') === false) {
            return;
          }

          var addStr = "<span class='button add' id='addBtn_" + treeNode.tId + "' title='添加' onfocus='this.blur();'></span>";
          $sObj.after(addStr);
          var $btn = $("#addBtn_" + treeNode.tId);

          if ($btn) {
            $btn.bind("click", function () {
              var base = !!(treeNode.children) ? treeNode.children.length : 0;
              var data = {
                  treeId: treeNode.id + '-' + base,
                  treeParentId: treeNode.id,
                  orderIndex : base,
                  name: "new node " + (treeNode.id + '-' + base),
                  url: '',
                  description: '',
                  isLeaf : 1
              };
              self.showLoading();
              MenuAPIS.addMenuNode(data)
                .then(resp => {
                  var bId = resp.sdata.bId;
                  var newNode = zTree.addNodes(treeNode, {
                      bId : bId,
                      id: data.treeId,
                      pId: treeNode.id,
                      name: data.name
                  });
                  zTree.editName(zTree.getNodeByTId(newNode.tId));
                })
                .finally(() => self.hideLoading());
              return false;
            });
          }
        };

        function removeHoverDom(treeId, treeNode) {
            $("#addBtn_" + treeNode.tId).unbind().remove();
        };

        var setting = {
          view: {
              addHoverDom: addHoverDom,
              removeHoverDom: removeHoverDom,
              selectedMulti: false
          },
          edit: {
              enable: true,
              editNameSelectAll: true,
              showRemoveBtn: showRemoveBtn,
              showRenameBtn: showRenameBtn
          },
          data: {
            simpleData: {
              enable: true
            }
          },
          callback: {
              beforeDrag: function (treeId, treeNodes) {
                  if(self.$$hasAuth('menu-move') === false) {
                    return false;
                  }
                  // console.log('before drag...');
                  for (var i = 0, l = treeNodes.length; i < l; i++) {
                      if (treeNodes[i].drag === false) {
                          return false;
                      }
                  }
                  var maxLevel = 0;
                  for(var i = 0; i < treeNodes.length; i++) {
                      maxLevel = maxLevel >= treeNodes[i].level ? maxLevel : treeNodes[i].level;
                  }
                  if(maxLevel > MAX_LEVEL) {
                      return false;
                  }
                  return true;
              },

              beforeDrop: function (treeId, treeNodes, targetNode, moveType) {
                var maxLevel = treeNodes[0].level;
                var minLevel = treeNodes[0].level;
                for(var i = 1; i < treeNodes.length; i++) {
                    maxLevel = maxLevel >= treeNodes[i].level ? maxLevel : treeNodes[i].level;
                    minLevel = minLevel <= treeNodes[i].level ? minLevel : treeNodes[i].level;
                }
                var deep = maxLevel - minLevel + 1;
                var t = targetNode.level + deep + (moveType === 'inner' ? 0 : -1)
                // 不能超过深度或者与根节点平级
                if(t > MAX_LEVEL || t <= 0) {
                    return false;
                }
                var result = targetNode ? targetNode.drop !== false : true;
                if(!result) {
                  return false;
                }

                return confirm('该移动操作将会保存至数据库，确定移动该节点吗？');
              },

              onDrop : function (treeId, treeNodes, targetNode, moveType) {
                var nodeInfos = generateTreeNodeInfo();
                var needRefershInfos = [];
                for(var i = 0; i < nodeInfos.length; i++) {
                    var tInfo = nodeInfos[i];
                    if(tInfo.id !== tInfo.oldId
                      || tInfo.pId !== tInfo.oldPId
                      || tInfo.isLeaf !== tInfo.isLeaf
                      || tInfo.orderIndex !== tInfo.oldOrderIndex) {
                      needRefershInfos.push(tInfo);
                    }
                }
                var adjusts = $.map(needRefershInfos, function (n, i) {
                    return n.bId + ',' + n.id + ',' + n.pId + ',' + n.isLeaf + ',' + n.orderIndex;
                });

                if(adjusts.length <= 0) {
                  return;
                }

                self.showLoading();
                MenuAPIS.adjustMenuNodes(adjusts.join(';'))
                  .then(resp => {
                    self.$message.success('节点位置调整数据同步成功');
                  })
                  .finally(() => self.hideLoading())
                  .then(() => {
                    self.checkedNodeIds = nodeInfos.filter(node => node.open).map(node => node.id);
                    self.init();
                  });
              },
              beforeEditName: beforeEditName,
              beforeRemove: beforeRemove,
              beforeRename: beforeRename,
              onRemove: onRemove,
              onRename: onRename,
              onClick: function (event, treeId, treeNode) {
                  if(treeNode.isParent) {
                    self.$emit('select', {treeNode : {bId : undefined}});
                  } else {
                    self.$emit('select', {treeNode : treeNode});
                  }
              }
          }
        };

        var zNodes = [{id: 1, pId: 0, name: "菜单管理", open: true, drag: false}].concat(treeNodes);
        $.fn.zTree.init($("#menu-tree"), setting, zNodes);
        zTree = $.fn.zTree.getZTreeObj("menu-tree");
        self.zTree = zTree;
      },

      reloadMenuNode (rd) {
        if(!rd.$hook) {
          return;
        }
        this.showLoading();
        MenuAPIS.queryMenu(this.editData.menuId)
          .then(resp => resp.sdata.menu)
          .then(menu => {
            this.editData.treeNode.name = menu.name;
            this.editData.treeNode.url = menu.url;
            this.editData.treeNode.description = menu.description;
            this.zTree.updateNode(this.editData.treeNode);
            this.$message.success('菜单节点更新成功');
          })
          .finally(() => this.hideLoading());
      },

      init () {
        this.showLoading();
        this.fetchMenuData()
          .then(menus => this.initTree(menus, this.checkedNodeIds))
          .finally(() => this.hideLoading());
      },
    }
  });
});
</script>

<style>
.ztree {
  position: relative;
}
.ztree li span.button.add {
  margin-left:2px;
  margin-right: -1px;
  background-position:-144px 0;
  vertical-align:top;
  *vertical-align:middle

}
#menu-tree {
  width: 100%;
  min-height: 600px;
  border: none;
}
</style>
