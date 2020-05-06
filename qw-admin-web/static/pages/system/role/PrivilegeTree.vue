<template>
  <span>
    <a-spin :spinning="isLoading">
      <div id="privilege-tree" class="ztree"></div>
    </a-spin>
  </span>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'mixins',
  'zTree',
  'jquery',
  'alias!@API/system/menu',
  'alias!@API/system/privilege',
  'alias!@PAGE/system/role/trees',
  'css!../../assets/libs/ztree/zTreeStyle/zTreeStyle.css'
], function(MXS, _zTree, $, MenuAPIS, PrivilegeAPIS, trees, __cssText, resolve, reject) {
  resolve({
    name: 'PrivilegeTree',
    mixins: [MXS.CommonMixin, MXS.AuthMixin],
    props: ['menuId'],
    data () {
      return {
        isEdit: false,
        editData: { },
        selectedPnames: []
      }
    },
    computed: {
      canBeChecked () {
        return this.$$hasAuth('menu-add-menu-privilege') && !!this.menuId;
      }
    },
    watch : {
      menuId (to, from) {
        this.fetchPrivilegeNames(to);
      },
      canBeChecked (to, from) {
        this.init()
          .then(this.fetchPrivilegeNames);
      }
    },
    mounted () {
      this.$$fetchAuth([
        'menu-add-menu-privilege'
      ]).then(() => this.init());
    },
    methods: {
      fetchPrivilegeData () {
        return PrivilegeAPIS.queryPrivileges()
          .then(resp => {
            let privileges = resp.sdata.privileges||[];
            return privileges;
          });
      },

      initTree (privileges) {
        var self = this;

        privileges = trees.convert(privileges, (i, n) => {
          return {
              bId : n.id,
              id : n.treeId,
              pId : n.treeParentId,
              orderIndex : n.orderIndex,
              key: n.name,
              name : n.description,
              urls : n.urls,
              description : n.description,
              chkDisabled: !this.canBeChecked
          };
        });
        var zTree = undefined;
        var setting = {
          view: {
              // 展开树节点没有延迟, 否则异步加载选择的节点由于首先收缩所有节点, 有延迟，加载完成之后的节点需要展开,
              // 两个异步事件同时处理会存在树节点展开不正确的问题.
              expandSpeed: 0,
              addHoverDom : this.onHoverDom,
              removeHoverDom : () => { },
              selectedMulti: false
          },
          check: {
              enable: true
          },
          data: {
              simpleData: {
                enable: true
              }
          },
          callback: {
            onCheck: this.onCheck
          }
        };

        var zNodes = [{ id:1, pId:0, name:"权限管理", open:true}].concat(privileges);

        $.fn.zTree.init($("#privilege-tree"), setting, zNodes);
        zTree = $.fn.zTree.getZTreeObj("privilege-tree");
        this.zTree = zTree;
      },

      onHoverDom(treeId, treeNode) {
        var $sObj = $('#' + treeNode.tId + '_a');
        var title = '';
        var urls = treeNode.urls;
        var description = treeNode.description;
        if(!!urls && urls.trim() != '') {
            var tArr = urls.split(/\s*,\s*/);
            urls = '路径信息:\r\n\t' + tArr.join('\r\n\t');
            title += urls + '\r\n';
        }
        if(!!description) {
            var tArr = description.split(/\s*,\s*/);
            description = '描述信息:\r\n\t' + tArr.join('\r\n\t');
            title += description;
        }
        $sObj.attr('title', title);
      },

      onCheck(e, treeId, treeNode) {
        // 无选择的菜单节点 or 无修改菜单节点权限
        // if(!this.menuId || this.$$hasAuth('menu-add-menu-privilege') === false) {
        //     this.fetchPrivilegeNames(menuId);
        //     return;
        // }
        var checkedNodes = this.zTree.getCheckedNodes(true);
        var tArr = [];
        for(var i = 0; i < checkedNodes.length; i++) {
            var tNode = checkedNodes[i];
            if(!tNode.isParent) {
                tArr.push(tNode.key);
            }
        }
        var privilegeNames = tArr.join(',');
        
        var formData = {
            menuId : this.menuId,
            privilegeNames : privilegeNames
        };
        this.showLoading();

        MenuAPIS.editMenuPrivileges(formData)
          .then(resp => {
            this.$message.success('菜单权限更新成功');
          })
          .finally(resp => this.hideLoading());
      },

      fetchPrivilegeNames(menuId) {
        if(!menuId) {
          this.checkNodeByNames(this.selectedPnames);
          return;
        }

        PrivilegeAPIS.queryNamesByMenuId(menuId)
          .then(resp => resp.sdata.pnames)
          .then(pnames => {
            this.selectedPnames = pnames;
            this.checkNodeByNames(pnames);
          });
      },

      checkNodeByNames(pnames) {
        this.zTree.checkAllNodes(false);
        this.zTree.expandAll(false);
        var root = this.zTree.getNodesByFilter(function (node) { return node.level == 0 }, true);
        this.zTree.expandNode(root, true, false, false, false);

        if(!Array.isArray(pnames) || pnames.length <= 0) {
          return;
        }

        var needCheckedNodes = this.zTree.getNodesByFilter(function (node) {
            return $.inArray(node.key, pnames) >= 0;
        }, false);
        for(var i = 0; !!needCheckedNodes && i < needCheckedNodes.length; i++) {
            var needCheckedNode = needCheckedNodes[i];
            // 自动展开, 不触发回调函数
            this.zTree.expandNode(needCheckedNode, true, false, false, false);
            // 展开所有父节点
            var tParentNode = needCheckedNode.getParentNode();
            while (!!tParentNode) {
                this.zTree.expandNode(tParentNode, true, false, false, false);
                tParentNode = tParentNode.getParentNode();
            }
            // 自动选择, 不触发回调函数
            if(needCheckedNode.chkDisabled) {
              this.zTree.setChkDisabled(needCheckedNode, false);
              this.zTree.checkNode(needCheckedNode, true, true, false);
              this.zTree.setChkDisabled(needCheckedNode, true);
            } else {
              this.zTree.checkNode(needCheckedNode, true, true, false);
            }
        }
      },

      init () {
        this.showLoading();
        return this.fetchPrivilegeData()
          .then(menus => this.initTree(menus))
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
#privilege-tree {
  width: 100%;
  min-height: 600px;
  border: none;
}
</style>
