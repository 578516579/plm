#!/usr/bin/env bash
# =============================================================================
# 业务模块生成器 (proposal 0034 落地)
#
# 用法:
#   bash plm-backend/scripts/new-business-module.sh <module> <Entity> <中文名> [parent_id]
#
# 示例:
#   bash plm-backend/scripts/new-business-module.sh feedback Feedback 用户反馈
#
# 会生成:
#   plm-backend/plm-<module>/                    完整 Maven 模块 (pom + 6 src + xml)
#   plm-frontend/packages/plm-<module>/          完整 npm package (api + types + views + router)
#   plm-frontend/src/{api,types/api,views}/business/<module>/  Legacy Stage 1 极薄壳
#   plm-backend/sql/business-<module>.sql        DDL + 字典 + 菜单
#   plm-frontend/e2e/<module>.spec.ts            8 case 基础 E2E
#
# 不会做（需要手动）:
#   - 实际填字段定义 (要根据业务设计填 Domain 字段)
#   - 修改 parent pom.xml 加 module + dependencyManagement (手动添加)
#   - 修改 plm-admin/pom.xml 加依赖
#   - 走 Phase 01-03 Gate 文档
#
# 关联:
#   - 03-开发/模块拆分指南.md §3 新 stub 启动流程
#   - 03-开发/Legacy-镜像迁移-playbook.md Stage 1 极薄壳模式
# =============================================================================
set -e

if [ $# -lt 3 ]; then
    echo "用法: $0 <module> <Entity> <中文名> [parent_menu_id]"
    echo "示例: $0 feedback Feedback 用户反馈 2000"
    exit 1
fi

MODULE="$1"          # feedback (小写)
ENTITY="$2"          # Feedback (PascalCase)
CN_NAME="$3"         # 用户反馈
PARENT_MENU_ID="${4:-2000}"   # 默认挂业务管理 2000 下

ENTITY_LOWER=$(echo "$ENTITY" | tr '[:upper:]' '[:lower:]')
# Java 包名不能含 hyphen → manual-product → manualproduct
PKG_LEAF=$(echo "$MODULE" | sed 's/-//g')
# DB 表名沿用 module (短横线允许,但部分 RDBMS 不爱,sed 改下划线更安全)
TABLE="tb_$(echo "$MODULE" | sed 's/-/_/g')"
PKG_BASE="cn/com/bosssfot/dv/plm/$PKG_LEAF"
PKG_DOT="cn.com.bosssfot.dv.plm.$PKG_LEAF"
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"

echo "=== 生成 plm-$MODULE 模块 ==="
echo "  module=$MODULE  entity=$ENTITY  CN=$CN_NAME  parent_menu=$PARENT_MENU_ID"
echo "  ROOT=$ROOT"
echo

# ============================================================
# 1. 后端 Maven 模块
# ============================================================
BACKEND="$ROOT/plm-backend/plm-$MODULE"
# brace expansion 在某些 sh 不支持,显式列出
mkdir -p "$BACKEND/src/main/java/$PKG_BASE/domain"
mkdir -p "$BACKEND/src/main/java/$PKG_BASE/mapper"
mkdir -p "$BACKEND/src/main/java/$PKG_BASE/service/impl"
mkdir -p "$BACKEND/src/main/java/$PKG_BASE/controller"
mkdir -p "$BACKEND/src/main/resources/mapper/$PKG_LEAF"

# pom.xml
cat > "$BACKEND/pom.xml" <<XML
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <parent>
        <artifactId>plm</artifactId>
        <groupId>cn.com.bosssfot.dv.plm</groupId>
        <version>3.9.2</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>plm-$MODULE</artifactId>
    <description>${CN_NAME}业务模块 (生成器输出, 需补 Domain 字段 + 业务规则)</description>
    <dependencies>
        <dependency><groupId>cn.com.bosssfot.dv.plm</groupId><artifactId>plm-common</artifactId></dependency>
        <dependency><groupId>cn.com.bosssfot.dv.plm</groupId><artifactId>plm-system</artifactId></dependency>
        <dependency><groupId>cn.com.bosssfot.dv.plm</groupId><artifactId>plm-project</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    </dependencies>
</project>
XML

# README.md
cat > "$BACKEND/README.md" <<MD
# plm-$MODULE

| 字段 | 值 |
|---|---|
| 模块中文名 | ${CN_NAME} |
| 状态 | **scaffold** (生成器输出, 待补字段 + 业务规则) |
| 后端 Maven | plm-$MODULE |
| 前端 package | @plm/$MODULE |
| DB 表 | $TABLE |
| Java 包 | $PKG_DOT |

## 后续步骤

1. 编辑 Domain.java 加自己的字段
2. 编辑 Mapper.xml 加对应 SQL
3. 编辑 ServiceImpl 加业务规则 (状态机 / FK 校验等)
4. 编辑 SQL 改字典/菜单
5. 走 Phase 01-03 Gate 走 R&D 流程
MD

# Domain (脚手架)
cat > "$BACKEND/src/main/java/$PKG_BASE/domain/$ENTITY.java" <<JAVA
package $PKG_DOT.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/** ${CN_NAME}对象 $TABLE (生成器脚手架,需补字段) */
public class $ENTITY extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long ${ENTITY_LOWER}Id;
    @Excel(name = "编号") private String ${ENTITY_LOWER}No;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "标题") private String title;
    @Excel(name = "状态") private String status;
    private String delFlag;

    public Long get${ENTITY}Id() { return ${ENTITY_LOWER}Id; }
    public void set${ENTITY}Id(Long v) { this.${ENTITY_LOWER}Id = v; }
    public String get${ENTITY}No() { return ${ENTITY_LOWER}No; }
    public void set${ENTITY}No(String v) { this.${ENTITY_LOWER}No = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("${ENTITY_LOWER}Id", ${ENTITY_LOWER}Id)
            .append("${ENTITY_LOWER}No", ${ENTITY_LOWER}No)
            .append("projectId", projectId)
            .append("title", title)
            .append("status", status)
            .toString();
    }
}
JAVA

# Mapper
cat > "$BACKEND/src/main/java/$PKG_BASE/mapper/${ENTITY}Mapper.java" <<JAVA
package $PKG_DOT.mapper;

import java.util.List;
import $PKG_DOT.domain.$ENTITY;

public interface ${ENTITY}Mapper {
    List<$ENTITY> select${ENTITY}List($ENTITY ${ENTITY_LOWER});
    $ENTITY select${ENTITY}ById(Long ${ENTITY_LOWER}Id);
    int insert${ENTITY}($ENTITY ${ENTITY_LOWER});
    int update${ENTITY}($ENTITY ${ENTITY_LOWER});
    int delete${ENTITY}ByIds(Long[] ${ENTITY_LOWER}Ids);
}
JAVA

# Mapper.xml
cat > "$BACKEND/src/main/resources/mapper/$PKG_LEAF/${ENTITY}Mapper.xml" <<XML
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="$PKG_DOT.mapper.${ENTITY}Mapper">
    <resultMap type="$ENTITY" id="${ENTITY}Result">
        <result property="${ENTITY_LOWER}Id" column="${ENTITY_LOWER}_id" />
        <result property="${ENTITY_LOWER}No" column="${ENTITY_LOWER}_no" />
        <result property="projectId"  column="project_id" />
        <result property="title"      column="title" />
        <result property="status"     column="status" />
        <result property="createBy"   column="create_by" />
        <result property="createTime" column="create_time" />
        <result property="updateBy"   column="update_by" />
        <result property="updateTime" column="update_time" />
        <result property="remark"     column="remark" />
        <result property="delFlag"    column="del_flag" />
    </resultMap>

    <sql id="select${ENTITY}Vo">
        select ${ENTITY_LOWER}_id, ${ENTITY_LOWER}_no, project_id, title, status,
               create_by, create_time, update_by, update_time, remark, del_flag
        from $TABLE
    </sql>

    <select id="select${ENTITY}List" parameterType="$ENTITY" resultMap="${ENTITY}Result">
        <include refid="select${ENTITY}Vo"/>
        <where>
            del_flag = '0'
            <if test="title != null and title != ''">and title like concat('%', #{title}, '%')</if>
            <if test="projectId != null">and project_id = #{projectId}</if>
            <if test="status != null and status != ''">and status = #{status}</if>
        </where>
        order by create_time desc
    </select>

    <select id="select${ENTITY}ById" parameterType="Long" resultMap="${ENTITY}Result">
        <include refid="select${ENTITY}Vo"/>
        where ${ENTITY_LOWER}_id = #{${ENTITY_LOWER}Id} and del_flag = '0'
    </select>

    <insert id="insert${ENTITY}" parameterType="$ENTITY" useGeneratedKeys="true" keyProperty="${ENTITY_LOWER}Id">
        insert into $TABLE (${ENTITY_LOWER}_no, project_id, title, status, create_by, create_time)
        values (#{${ENTITY_LOWER}No}, #{projectId}, #{title}, #{status}, #{createBy}, sysdate())
    </insert>

    <update id="update${ENTITY}" parameterType="$ENTITY">
        update $TABLE set title = #{title}, status = #{status}, update_by = #{updateBy}, update_time = sysdate()
        where ${ENTITY_LOWER}_id = #{${ENTITY_LOWER}Id}
    </update>

    <update id="delete${ENTITY}ByIds" parameterType="Long">
        update $TABLE set del_flag = '2' where ${ENTITY_LOWER}_id in
        <foreach item="id" collection="array" open="(" separator="," close=")">#{id}</foreach>
    </update>
</mapper>
XML

# IService
cat > "$BACKEND/src/main/java/$PKG_BASE/service/I${ENTITY}Service.java" <<JAVA
package $PKG_DOT.service;

import java.util.List;
import $PKG_DOT.domain.$ENTITY;

public interface I${ENTITY}Service {
    List<$ENTITY> select${ENTITY}List($ENTITY ${ENTITY_LOWER});
    $ENTITY select${ENTITY}ById(Long ${ENTITY_LOWER}Id);
    int insert${ENTITY}($ENTITY ${ENTITY_LOWER});
    int update${ENTITY}($ENTITY ${ENTITY_LOWER});
    int delete${ENTITY}ByIds(Long[] ${ENTITY_LOWER}Ids);
}
JAVA

# ServiceImpl
cat > "$BACKEND/src/main/java/$PKG_BASE/service/impl/${ENTITY}ServiceImpl.java" <<JAVA
package $PKG_DOT.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import $PKG_DOT.domain.$ENTITY;
import $PKG_DOT.mapper.${ENTITY}Mapper;
import $PKG_DOT.service.I${ENTITY}Service;

/** ${CN_NAME} Service - 脚手架,需补业务规则 (状态机 / FK 校验 / 编号生成) */
@Service
public class ${ENTITY}ServiceImpl implements I${ENTITY}Service {

    @Autowired private ${ENTITY}Mapper ${ENTITY_LOWER}Mapper;

    @Override public List<$ENTITY> select${ENTITY}List($ENTITY t) { return ${ENTITY_LOWER}Mapper.select${ENTITY}List(t); }
    @Override public $ENTITY select${ENTITY}ById(Long id) { return ${ENTITY_LOWER}Mapper.select${ENTITY}ById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert${ENTITY}($ENTITY t) {
        // TODO: 加字段校验 + FK 校验 + 编号生成 + 状态约束
        t.setCreateBy(SecurityUtils.getUsername());
        return ${ENTITY_LOWER}Mapper.insert${ENTITY}(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update${ENTITY}($ENTITY t) {
        // TODO: 加状态机 + FK re-check
        t.setUpdateBy(SecurityUtils.getUsername());
        return ${ENTITY_LOWER}Mapper.update${ENTITY}(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete${ENTITY}ByIds(Long[] ids) {
        return ${ENTITY_LOWER}Mapper.delete${ENTITY}ByIds(ids);
    }
}
JAVA

# Controller
cat > "$BACKEND/src/main/java/$PKG_BASE/controller/${ENTITY}Controller.java" <<JAVA
package $PKG_DOT.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;
import $PKG_DOT.domain.$ENTITY;
import $PKG_DOT.service.I${ENTITY}Service;

@RestController
@RequestMapping("/business/$MODULE")
public class ${ENTITY}Controller extends BaseController {

    @Autowired
    private I${ENTITY}Service ${ENTITY_LOWER}Service;

    @PreAuthorize("@ss.hasPermi('business:$MODULE:list')")
    @GetMapping("/list")
    public TableDataInfo list($ENTITY ${ENTITY_LOWER}) {
        startPage();
        return getDataTable(${ENTITY_LOWER}Service.select${ENTITY}List(${ENTITY_LOWER}));
    }

    @PreAuthorize("@ss.hasPermi('business:$MODULE:export')")
    @Log(title = "${CN_NAME}", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, $ENTITY ${ENTITY_LOWER}) {
        List<$ENTITY> list = ${ENTITY_LOWER}Service.select${ENTITY}List(${ENTITY_LOWER});
        new ExcelUtil<$ENTITY>($ENTITY.class).exportExcel(response, list, "${CN_NAME}数据");
    }

    @PreAuthorize("@ss.hasPermi('business:$MODULE:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(${ENTITY_LOWER}Service.select${ENTITY}ById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:$MODULE:add')")
    @Log(title = "${CN_NAME}", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody $ENTITY ${ENTITY_LOWER}) {
        return toAjax(${ENTITY_LOWER}Service.insert${ENTITY}(${ENTITY_LOWER}));
    }

    @PreAuthorize("@ss.hasPermi('business:$MODULE:edit')")
    @Log(title = "${CN_NAME}", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody $ENTITY ${ENTITY_LOWER}) {
        return toAjax(${ENTITY_LOWER}Service.update${ENTITY}(${ENTITY_LOWER}));
    }

    @PreAuthorize("@ss.hasPermi('business:$MODULE:remove')")
    @Log(title = "${CN_NAME}", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(${ENTITY_LOWER}Service.delete${ENTITY}ByIds(ids));
    }
}
JAVA

# SQL
SQL_FILE="$ROOT/plm-backend/sql/business-$MODULE.sql"
cat > "$SQL_FILE" <<SQL
-- ${CN_NAME}业务模块 DDL (生成器脚手架,需扩展字段)

DROP TABLE IF EXISTS $TABLE;
CREATE TABLE $TABLE (
    ${ENTITY_LOWER}_id   BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '主键',
    ${ENTITY_LOWER}_no   VARCHAR(32)  NOT NULL                 COMMENT '编号',
    project_id           BIGINT(20)   NOT NULL                 COMMENT 'FK→tb_project',
    title                VARCHAR(200) NOT NULL                 COMMENT '标题',
    status               VARCHAR(2)   NOT NULL DEFAULT '00'    COMMENT '状态',
    create_by            VARCHAR(64)  DEFAULT '',
    create_time          DATETIME     DEFAULT NULL,
    update_by            VARCHAR(64)  DEFAULT '',
    update_time          DATETIME     DEFAULT NULL,
    remark               VARCHAR(500) DEFAULT '',
    del_flag             CHAR(1)      DEFAULT '0',
    PRIMARY KEY (${ENTITY_LOWER}_id),
    UNIQUE KEY uk_${PKG_LEAF}_no (${ENTITY_LOWER}_no),
    KEY idx_${PKG_LEAF}_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='${CN_NAME}';

-- 菜单 (调整 menu_id 段, 当前 9000+ 是脚手架占位)
-- INSERT INTO sys_menu ... ;
-- INSERT INTO sys_role_menu ... ;
SQL

echo "  ✓ 后端 plm-$MODULE (pom + 6 src + xml + sql)"

# ============================================================
# 2. 前端 @plm/<module> package
# ============================================================
FRONTEND="$ROOT/plm-frontend/packages/plm-$MODULE"
mkdir -p "$FRONTEND/src/api" "$FRONTEND/src/types" "$FRONTEND/src/views"

cat > "$FRONTEND/package.json" <<JSON
{
  "name": "@plm/$MODULE",
  "version": "0.4.0",
  "description": "${CN_NAME} (生成器输出)",
  "main": "src/index.ts",
  "type": "module",
  "private": true,
  "exports": {
    ".": "./src/index.ts",
    "./api": "./src/api/index.ts",
    "./types": "./src/types/index.ts",
    "./router": "./src/router.ts"
  }
}
JSON

cat > "$FRONTEND/README.md" <<MD
# @plm/$MODULE

${CN_NAME} (生成器输出,需补 views/api 业务逻辑)
MD

cat > "$FRONTEND/src/router.ts" <<TS
import type { RouteRecordRaw } from 'vue-router'
const routes: RouteRecordRaw[] = [{
  path: '/business/$MODULE',
  name: '$ENTITY',
  component: () => import('./views/index.vue'),
  meta: { title: '${CN_NAME}', permi: 'business:$MODULE:list' }
}]
export default routes
TS

cat > "$FRONTEND/src/index.ts" <<TS
export * from './api/index'
export * from './types/index'
export { default as routes } from './router'
TS

cat > "$FRONTEND/src/api/index.ts" <<TS
import request from '@/utils/request'
import type { ${ENTITY}Query, ${ENTITY}Form } from '../types'

export function list${ENTITY}(query: ${ENTITY}Query) {
  return request({ url: '/business/$MODULE/list', method: 'get', params: query })
}
export function get${ENTITY}(id: number | string) {
  return request({ url: '/business/$MODULE/' + id, method: 'get' })
}
export function add${ENTITY}(data: ${ENTITY}Form) {
  return request({ url: '/business/$MODULE', method: 'post', data })
}
export function update${ENTITY}(data: ${ENTITY}Form) {
  return request({ url: '/business/$MODULE', method: 'put', data })
}
export function del${ENTITY}(ids: (number | string)[]) {
  return request({ url: '/business/$MODULE/' + ids.join(','), method: 'delete' })
}
TS

cat > "$FRONTEND/src/types/index.ts" <<TS
import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface ${ENTITY}Form extends BaseEntity {
  ${ENTITY_LOWER}Id?: number | string
  ${ENTITY_LOWER}No?: string
  projectId?: number | string
  title?: string
  status?: string
}

export interface ${ENTITY}Query extends PageQuery {
  ${ENTITY_LOWER}No?: string
  projectId?: number | string
  title?: string
  status?: string
}
TS

cat > "$FRONTEND/src/views/index.vue" <<VUE
<template>
  <div class="app-container">
    <el-result icon="info" title="${CN_NAME}" sub-title="生成器输出, views 待补充业务交互">
      <template #extra><el-button type="primary" @click="\$router.back()">返回</el-button></template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: '$ENTITY' })
</script>
VUE

# Legacy Stage 1 极薄壳
mkdir -p "$ROOT/plm-frontend/src/views/business/$MODULE"
cat > "$ROOT/plm-frontend/src/api/business/$MODULE.ts" <<TS
/** 极薄壳 (Stage 1) */
export * from '../../../packages/plm-$MODULE/src/api/index'
TS
cat > "$ROOT/plm-frontend/src/types/api/business/$MODULE.ts" <<TS
/** 极薄壳 */
export * from '../../../../packages/plm-$MODULE/src/types/index'
TS
cat > "$ROOT/plm-frontend/src/views/business/$MODULE/index.vue" <<VUE
<template><View /></template>
<script setup lang="ts">
import View from '../../../../packages/plm-$MODULE/src/views/index.vue'
</script>
VUE

echo "  ✓ 前端 @plm/$MODULE + Legacy 极薄壳"

# ============================================================
# 3. E2E 脚手架
# ============================================================
cat > "$ROOT/plm-frontend/e2e/$MODULE.spec.ts" <<TS
/** $ENTITY 模块 E2E (生成器脚手架, 待补业务规则测试) */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string
let api: ApiClient
let apiRequest: APIRequestContext
let projectId: number

test.describe('$ENTITY 模块 E2E (脚手架)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(\`$MODULE-suite-\${RUN_ID}\`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(\`$MODULE-suite-\${RUN_ID}\`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('$TABLE', \`project_id=\${projectId}\`)
      execDelete('tb_project', \`id=\${projectId}\`)
    }
    await apiRequest?.dispose()
  })

  test('TC-$ENTITY-F001 CRUD 脚手架', async () => {
    const c = await api.post('/business/$MODULE', {
      ${ENTITY_LOWER}No: \`SCAFFOLD-\${RUN_ID}\`,
      projectId,
      title: \`生成器测试-\${RUN_ID}\`,
      status: '00'
    })
    expect(c.code).toBe(200)
  })
})
TS

echo "  ✓ E2E e2e/$MODULE.spec.ts"

# ============================================================
# 4. 收尾提示
# ============================================================
echo
echo "============================================================"
echo "✅ plm-$MODULE 生成完毕!"
echo "============================================================"
echo
echo "手动后续 (5 步):"
echo
echo "1. 编辑 plm-backend/pom.xml:"
echo "   - 在 <modules> 加 <module>plm-$MODULE</module>"
echo "   - 在 <dependencyManagement> 加 plm-$MODULE 依赖声明"
echo
echo "2. 编辑 plm-backend/plm-admin/pom.xml:"
echo "   - 加 plm-$MODULE 依赖"
echo
echo "3. 补 SQL 字典 + 菜单 (plm-backend/sql/business-$MODULE.sql):"
echo "   - 加 sys_dict_type / sys_dict_data / sys_menu / sys_role_menu inserts"
echo
echo "4. 补 Domain.java 字段 + ServiceImpl 业务规则 (状态机/FK 校验/编号生成)"
echo
echo "5. 走 Phase 01-03 Gate:"
echo "   - 写 01-立项/${ENTITY}-PRD.md"
echo "   - 写 02-设计/${ENTITY}-{数据库设计,API设计}.md"
echo "   - 写 99-跨阶段/gate-checklists/instances/$MODULE/Phase0{1,2,3}-...md"
echo
echo "6. mvn install -T 4 + 跑 E2E + commit"
echo
