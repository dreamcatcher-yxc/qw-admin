<template>
  <span>
    <a-spin :spinning="isLoading">
      <div id="role-privilege-tree" class="ztree"></div>
    </a-spin>
  </span>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'mixins',
  'zTree',
  'jquery',
  'lodash',
  'alias!@API/system/rolePrivilege',
  'alias!@PAGE/system/role/trees',
  'css!../../assets/libs/ztree/zTreeStyle/zTreeStyle.css'
], function(MXS, _zTree, $, _, RpAPIS, trees, __cssText, resolve, reject) {
  resolve({
    name: 'RolePrivilegeTree',
    mixins: [MXS.CommonMixin, MXS.AuthMixin],
    props: ['roleId'],
    data () {
      return {
        // 标记当前选择的节点是否和原来的一致
        isChanged: false,
        // 原来选择的节点信息
        oldPnames: [],
        // 新选择的节点信息
        newPnames: []
      }
    },
    computed: {
      canBeChecked () {
        return this.$$hasAuth('role-assign-privilege');
      }
    },
    mounted () {
      this.$$fetchAuth([
        'role-assign-privilege'
      ]).then(() => {
        this.init();
      });
    },
    methods: {
      fetchPrivilegeData () {
        return RpAPIS.queryRolePrivileges(this.roleId)
          .then(resp => {
            let privileges = resp.sdata.allPrivileges||[];
            let pnames = resp.sdata.pnames||[];
            this.oldPnames = pnames;
            return { privileges: privileges, pnames: pnames };
          });
      },

      initTree (data) {
        var self = this;

        var privileges = data.privileges;
        var pnames = data.pnames;
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

        var zNodes = [{ id:1, pId:0, name:"权限树", open:true}].concat(privileges);

        $.fn.zTree.init($("#role-privilege-tree"), setting, zNodes);
        zTree = $.fn.zTree.getZTreeObj("role-privilege-tree");
        this.zTree = zTree;

        // 选择节点
        this.checkNodeByNames(pnames);
      },

      onHoverDom(treeId, treeNode) {
        var $sObj = $('#' + treeNode.tId + '_a');
        var title = '键值: \r\n\t' + treeNode.key + '\r\n';
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
        var checkedNodes = this.zTree.getCheckedNodes(true);
        var tArr = [];
        for(var i = 0; i < checkedNodes.length; i++) {
            var tNode = checkedNodes[i];
            if(!tNode.isParent) {
              tArr.push(tNode.key);
            }
        }
        
        this.isChanged = !((this.oldPnames.length === tArr.length) && (_.intersection(this.oldPnames, tArr).length === tArr.length));
  
        if(this.isChanged) {
          this.newPnames = tArr;
        } else {
          this.newPnames = [];
        }
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
        this.fetchPrivilegeData()
          .then(menus => this.initTree(menus))
          .finally(() => this.hideLoading());
      },

      /**
       * 提交修改的信息
       */
      submit() {
        if(!this.roleId || !this.isChanged) {
            return Promise.resolve(false);
        }

        let formData = {
            roleId : this.roleId,
            pnames : this.newPnames.join(',')
        };

        return RpAPIS.editRps(formData);
      }
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
#role-privilege-tree {
  width: 100%;
  min-height: 600px;
  border: none;
}
</style>
