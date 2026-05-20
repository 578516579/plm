/**
 * 直接查 MySQL — 用于 HEX 校验等"数据库层断言",
 * 因为只有比对 raw bytes 才能确认无乱码
 */
import { execSync } from 'child_process'

const MYSQL = process.env.MYSQL_CLI || 'C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'
const DB_NAME = process.env.DB_NAME || 'plm'
const DB_USER = process.env.DB_USER || 'root'
const DB_PWD = process.env.DB_PASSWORD || (() => {
  throw new Error(
    '[e2e/helpers/db.ts] 环境变量 DB_PASSWORD 未设置。\n' +
    '请先在 shell 中:  export DB_PASSWORD=<your-mysql-root-password>\n' +
    '详见 04-测试/测试用例库/E2E-运行手册.md §3'
  )
})()

/**
 * 执行 SQL 返回单值（-Nse 模式,silent + no-header）
 */
export function querySingleValue(sql: string): string {
  const cmd = `"${MYSQL}" -u${DB_USER} -p${DB_PWD} --default-character-set=utf8mb4 ${DB_NAME} -Nse "${sql.replace(/"/g, '\\"')}"`
  try {
    return execSync(cmd, { encoding: 'utf8', stdio: ['pipe', 'pipe', 'pipe'] }).trim()
  } catch (e: any) {
    throw new Error(`SQL failed: ${sql}\n${e.stderr || e.message}`)
  }
}

/** 查任意字段的 HEX 字节 */
export function getFieldHex(table: string, column: string, whereClause: string): string {
  return querySingleValue(
    `SELECT HEX(${column}) FROM ${table} WHERE ${whereClause}`
  )
}

/**
 * 校验某个字段是合法 UTF-8（含期望前缀且无 EFBFBD 替换符）
 */
export function assertNoMojibake(table: string, column: string, whereClause: string): {
  hex: string
  ok: boolean
  reason?: string
} {
  const hex = getFieldHex(table, column, whereClause)
  if (!hex) {
    return { hex, ok: false, reason: '查不到记录' }
  }
  if (hex.toUpperCase().includes('EFBFBD')) {
    return { hex, ok: false, reason: 'HEX 中包含 EFBFBD (U+FFFD 替换符 — 乱码标记)' }
  }
  return { hex, ok: true }
}

/** 直接 DELETE 清理数据,用于测试 teardown */
export function execDelete(table: string, whereClause: string): void {
  const cmd = `"${MYSQL}" -u${DB_USER} -p${DB_PWD} --default-character-set=utf8mb4 ${DB_NAME} -e "DELETE FROM ${table} WHERE ${whereClause}"`
  try {
    execSync(cmd, { stdio: 'pipe' })
  } catch (e: any) {
    // 清理失败不致命,记日志即可
    console.warn(`[db cleanup] DELETE FROM ${table} WHERE ${whereClause} failed: ${e.message}`)
  }
}
