

// ================================================================
// AgriPLM·AI — Complete JavaScript (Syntax-Safe Version)
// All template literals use helper functions to avoid nesting
// ================================================================

// ===== GLOBAL STATE =====
var state = {
  projects: [
    {id:'P001',name:'智慧灌溉决策平台 v2.1',biz:'精准农业',status:'研发中',progress:78,health:'green',owner:'王工',end:'2026-05-20'},
    {id:'P002',name:'农资电商小程序 v1.3',biz:'农资流通',status:'测试中',progress:45,health:'amber',owner:'李工',end:'2026-04-30'},
    {id:'P003',name:'病虫害AI识别模块',biz:'植保服务',status:'规划中',progress:22,health:'red',owner:'陈工',end:'2026-06-10'},
    {id:'P004',name:'农业溯源区块链平台',biz:'质量溯源',status:'验收中',progress:92,health:'green',owner:'赵工',end:'2026-04-25'}
  ],
  requirements: [
    {id:'REQ-089',title:'离线模式下土壤数据本地缓存策略',src:'客户反馈',pri:'P0',status:'待评审',ai:'高价值',desc:''},
    {id:'REQ-088',title:'灌溉推荐算法支持多作物品种参数',src:'内部提案',pri:'P1',status:'开发中',ai:'中价值',desc:''},
    {id:'REQ-087',title:'手机端灌溉历史数据图表展示',src:'客户反馈',pri:'P2',status:'已完成',ai:'低价值',desc:''},
    {id:'REQ-086',title:'农资SKU批量导入Excel功能',src:'运营数据',pri:'P1',status:'开发中',ai:'高价值',desc:''},
    {id:'REQ-085',title:'病虫害识别结果分享微信功能',src:'竞品分析',pri:'P2',status:'待评审',ai:'中价值',desc:''},
    {id:'REQ-084',title:'区块链溯源二维码批量生成',src:'客户反馈',pri:'P1',status:'已完成',ai:'高价值',desc:''}
  ],
  tasks: [
    {id:'TASK-124',title:'土壤墒情API数据接入模块',col:'待开发',pri:'P0',owner:'王工',hours:16,desc:''},
    {id:'TASK-125',title:'离线数据同步冲突处理逻辑',col:'待开发',pri:'P1',owner:'李工',hours:24,desc:''},
    {id:'TASK-126',title:'灌溉推荐算法单元测试',col:'待开发',pri:'P2',owner:'陈工',hours:8,desc:''},
    {id:'TASK-118',title:'灌溉决策模型集成Dify工作流',col:'开发中',pri:'P0',owner:'王工',hours:32,desc:''},
    {id:'TASK-119',title:'手机端灌溉控制页面开发',col:'开发中',pri:'P1',owner:'李工',hours:20,desc:''},
    {id:'TASK-115',title:'气象数据缓存策略优化',col:'代码评审',pri:'P1',owner:'陈工',hours:12,desc:''},
    {id:'TASK-112',title:'用户登录鉴权流程',col:'测试中',pri:'P0',owner:'赵工',hours:8,desc:''},
    {id:'TASK-101',title:'项目初始化与CI/CD配置',col:'已完成',pri:'P1',owner:'王工',hours:6,desc:''},
    {id:'TASK-102',title:'数据库初始化脚本',col:'已完成',pri:'P0',owner:'李工',hours:4,desc:''}
  ],
  testcases: [
    {id:'TC-0421',title:'弱网环境下土壤数据正常缓存至本地',type:'异常场景',pri:'P0',status:'通过',ai:true},
    {id:'TC-0422',title:'断网恢复后自动同步本地缓存到云端',type:'功能测试',pri:'P0',status:'失败',ai:true},
    {id:'TC-0423',title:'多设备同时上传数据的冲突解决',type:'边界测试',pri:'P1',status:'待执行',ai:true},
    {id:'TC-0424',title:'灌溉推荐在土壤含水率0%时的处理',type:'边界测试',pri:'P0',status:'待执行',ai:true},
    {id:'TC-0425',title:'正常灌溉推荐流程验证',type:'功能测试',pri:'P1',status:'通过',ai:true}
  ],
  defects: [
    {id:'BUG-156',title:'断网同步后数据出现重复记录',level:'P0',status:'修复中',owner:'王工',module:'同步模块',note:''},
    {id:'BUG-155',title:'土壤含水率超过100%时应用崩溃',level:'P0',status:'待确认',owner:'李工',module:'数据模型',note:''},
    {id:'BUG-154',title:'灌溉推荐页面在iPhone SE上布局错乱',level:'P2',status:'修复中',owner:'陈工',module:'移动端',note:''},
    {id:'BUG-153',title:'Dify工作流超时未处理异常',level:'P1',status:'待确认',owner:'王工',module:'AI引擎',note:''},
    {id:'BUG-152',title:'历史记录分页加载慢（>3s）',level:'P2',status:'已关闭',owner:'赵工',module:'数据列表',note:''}
  ],
  submitList: [
    {id:'ST-012',title:'智慧灌溉v2.1 Sprint3 提测',status:'待审批',submitter:'王工',date:'2026-04-14',gate:'通过'},
    {id:'ST-011',title:'农资小程序 Sprint5 提测',status:'测试中',submitter:'李工',date:'2026-04-10',gate:'通过'},
    {id:'ST-010',title:'溯源平台 Sprint7 提测',status:'已完成',submitter:'赵工',date:'2026-04-01',gate:'通过'}
  ],
  apiDesigns: [
    {id:'API-001',method:'GET',path:'/api/v1/irrigation/recommend',desc:'获取AI灌溉推荐方案',status:'已确认'},
    {id:'API-002',method:'POST',path:'/api/v1/sensor/data/upload',desc:'传感器数据批量上传',status:'已确认'},
    {id:'API-003',method:'GET',path:'/api/v1/weather/forecast',desc:'获取7日气象预报',status:'设计中'},
    {id:'API-004',method:'PUT',path:'/api/v1/irrigation/execute',desc:'执行灌溉指令',status:'设计中'},
    {id:'API-005',method:'GET',path:'/api/v1/field/list',desc:'获取地块列表',status:'已确认'},
    {id:'API-006',method:'POST',path:'/api/v1/offline/sync',desc:'离线数据同步接口',status:'设计中'}
  ],
  autoTests: [
    {id:'AT-001',name:'灌溉推荐接口测试套件',type:'接口测试',cases:24,lastRun:'2026-04-15 09:00',passRate:96,status:'就绪'},
    {id:'AT-002',name:'用户认证E2E测试',type:'端到端',cases:18,lastRun:'2026-04-15 09:00',passRate:88,status:'就绪'},
    {id:'AT-003',name:'离线同步功能测试',type:'功能测试',cases:32,lastRun:'2026-04-14 22:00',passRate:78,status:'失败'},
    {id:'AT-004',name:'性能压测-并发灌溉指令',type:'性能测试',cases:10,lastRun:'2026-04-13 18:00',passRate:100,status:'就绪'}
  ],
  sprints: [
    {id:'SP-001',name:'Sprint 1',start:'2026-02-01',days:14,goal:'基础架构搭建',status:'done',progress:100},
    {id:'SP-002',name:'Sprint 2',start:'2026-02-15',days:14,goal:'传感器数据接入',status:'done',progress:100},
    {id:'SP-003',name:'Sprint 3',start:'2026-03-01',days:14,goal:'灌溉推荐AI引擎',status:'active',progress:68},
    {id:'SP-004',name:'Sprint 4',start:'2026-04-15',days:14,goal:'离线同步与移动端',status:'planned',progress:0}
  ],
  messages: [
    {id:'MSG-001',title:'提测申请需要审批',body:'智慧灌溉v2.1 Sprint3 提测单待您审批',type:'warn',read:false,time:'10分钟前',page:'submit'},
    {id:'MSG-002',title:'AI缺陷检测提醒',body:'BUG-155（土壤含水率崩溃）已超过48小时未修复，P0紧急',type:'err',read:false,time:'1小时前',page:'defects'},
    {id:'MSG-003',title:'竞品动态：PingCode更新',body:'PingCode新增AI需求分析功能，威胁度高',type:'warn',read:false,time:'2小时前',page:'competitive'},
    {id:'MSG-004',title:'测试报告已生成',body:'Sprint3自动化测试完成，通过率89%',type:'info',read:true,time:'昨天',page:'testreport'},
    {id:'MSG-005',title:'PRD文档审批通过',body:'AI灌溉推荐引擎PRD已通过评审',type:'',read:true,time:'昨天',page:'prd'}
  ],
  testEnvs: [
    {id:'ENV-001',name:'开发环境 DEV',url:'dev.agri.local',type:'开发',owner:'王工',status:'online',lastCheck:'2026-04-15 10:00'},
    {id:'ENV-002',name:'测试环境 TEST',url:'test.agri.local',type:'测试',owner:'李工',status:'online',lastCheck:'2026-04-15 10:05'},
    {id:'ENV-003',name:'预发环境 PRE',url:'pre.agri.local',type:'预发',owner:'赵工',status:'offline',lastCheck:'2026-04-14 18:00'}
  ],
  kbDocs: [
    {id:'KB-001',name:'NY/T 1782-2021 农田灌溉水质标准',cat:'国家标准',size:'2.3MB',status:'已向量化',date:'2026-01-10'},
    {id:'KB-002',name:'水稻全生育期灌溉制度研究',cat:'科研文献',size:'1.8MB',status:'已向量化',date:'2026-02-05'},
    {id:'KB-003',name:'冬小麦节水灌溉技术规程',cat:'技术规程',size:'856KB',status:'已向量化',date:'2026-02-18'},
    {id:'KB-004',name:'土壤墒情监测技术规范',cat:'国家标准',size:'3.1MB',status:'已向量化',date:'2026-03-01'},
    {id:'KB-005',name:'农业病虫害识别图鉴（600种）',cat:'知识图谱',size:'45MB',status:'已向量化',date:'2026-03-10'}
  ],
  uedVersions: [
    {v:'v2.3',date:'2026-04-15',author:'设计师小林',status:'最新',desc:'修改灌溉控制页面布局'},
    {v:'v2.2',date:'2026-04-10',author:'设计师小林',status:'已归档',desc:'新增离线状态提示组件'},
    {v:'v2.1',date:'2026-04-01',author:'设计师小林',status:'已归档',desc:'首屏大屏组件优化'}
  ],
  savedPRDs: [
    {title:'AI灌溉推荐引擎',ver:'v1.2',status:'已确认',date:'2026-04-10'},
    {title:'农资电商购物车',ver:'v2.0',status:'评审中',date:'2026-04-05'}
  ],
  roles: [
    {role:'产品经理',perms:'立项✅ 竞品✅ 需求✅ PRD✅ 研发❌ 测试查看✅'},
    {role:'研发工程师',perms:'PRD查看✅ 研发✅ 测试协作✅ 文档查看✅'},
    {role:'测试工程师',perms:'需求查看✅ PRD查看✅ 测试全部✅ 缺陷✅'},
    {role:'项目经理',perms:'全部模块✅ 系统设置❌'},
    {role:'部门负责人',perms:'全部模块✅ 系统设置✅'}
  ],
  taskComments: {},
  taskMRs: {},
  selectedTCs: {},
  editingTaskId: null,
  editingReqId: null,
  currentSubmitId: null,
  currentRole: null,
  kbUploadFiles: [],
  mockInterval: null,
  mockReqCount: 0,
  mockCurrentPath: '',
  prdGenerated: false,
  archGenerated: false,
  dbGenerated: false
};

// ===== HTML HELPERS (avoid nested template literals) =====
function badge(cls, text) {
  return '<span class="b ' + cls + '">' + text + '</span>';
}
function btn(cls, label, onclick) {
  return '<button class="btn ' + cls + '" onclick="' + onclick + '">' + label + '</button>';
}
function progressBar(pct, colorClass) {
  return '<div class="pb"><div class="pf ' + colorClass + '" style="width:' + pct + '%"></div></div>';
}
function healthColor(h) {
  return h === 'green' ? '#10b981' : h === 'amber' ? 'var(--am)' : 'var(--rd)';
}
function healthBadge(h) {
  var m = {green:'🟢 健康', amber:'🟡 注意', red:'🔴 风险'};
  var c = {green:'bg', amber:'bam', red:'brd'};
  return badge(c[h] || 'bgr', m[h] || h);
}
function statusBadge(s) {
  var m = {研发中:'bbl', 测试中:'bam', 验收中:'bg', 规划中:'bgr', 已完成:'bg', 待评审:'bam', 开发中:'bbl', 待确认:'bam', 修复中:'bbl', 待验证:'bam', 已关闭:'bg', 已驳回:'brd'};
  return badge(m[s] || 'bgr', s);
}
function priLabel(p) {
  var m = {P0:'brd', P1:'bam', P2:'bgr'};
  return badge(m[p] || 'bgr', p);
}
function methodBadge(m) {
  var c = {GET:'m-get', POST:'m-post', PUT:'m-put', DELETE:'m-del', PATCH:'m-patch'};
  return '<span class="method ' + (c[m] || '') + '">' + m + '</span>';
}
function nowStr() {
  return new Date().toTimeString().slice(0,8);
}
function dateStr() {
  return new Date().toISOString().slice(0,10);
}

// ===== NOTIFICATION =====
function notify(msg, type) {
  var c = document.getElementById('notifContainer');
  if (!c) return;
  var d = document.createElement('div');
  d.className = 'notif-item ' + (type || '');
  var icon = type === 'err' ? '❌' : type === 'warn' ? '⚠️' : type === 'info' ? 'ℹ️' : '✅';
  d.innerHTML = '<div class="notif-title">' + icon + ' ' + (type === 'err' ? '错误' : type === 'warn' ? '警告' : type === 'info' ? '提示' : '成功') + '</div><div>' + msg + '</div>';
  c.appendChild(d);
  setTimeout(function() {
    d.style.animation = 'fadeOut .4s ease forwards';
    setTimeout(function() { if (d.parentNode) d.parentNode.removeChild(d); }, 400);
  }, 3500);
}

// ===== PAGE NAVIGATION =====
var PAGE_TITLES = {
  dashboard:'工作台', projects:'项目列表', inception:'项目立项', competitive:'竞品情报',
  requirements:'需求管理', prd:'AI PRD生成器', ued:'UED设计协同', archdesign:'系统概要设计',
  dbdesign:'数据库设计', apidesign:'接口详细设计', kanban:'研发看板', testplan:'测试方案计划',
  testcase:'测试用例', testdata:'测试数据工厂', submit:'提测管理', autotest:'自动化测试',
  defects:'缺陷管理', testreport:'测试报告', apidoc:'API文档', productmanual:'产品手册',
  implmanual:'实施手册', opsmanual:'运维手册', analytics:'效能分析', settings:'系统设置'
};

function showPage(name) {
  // In multi-file mode: navigate to the target page file
  window.location.href = name + '.html';
}

// Internal page activation (used by the page's own init script)
function activatePage(name) {
  document.querySelectorAll('.pg').forEach(function(p) { p.classList.remove('active'); });
  var pg = document.getElementById('pg-' + name);
  if (pg) pg.classList.add('active');
  document.querySelectorAll('.ni').forEach(function(n) {
    n.classList.remove('active');
    if (n.dataset.page === name) n.classList.add('active');
  });
  var t = document.getElementById('pgTitle');
  if (t) t.textContent = PAGE_TITLES[name] || name;
}

function renderPage(name) {
  if (name === 'dashboard') renderDashboard();
  else if (name === 'projects') renderProjects();
  else if (name === 'requirements') renderRequirements();
  else if (name === 'kanban') renderKanban();
  else if (name === 'testcase') renderTestCases();
  else if (name === 'defects') renderDefects();
  else if (name === 'submit') renderSubmit();
  else if (name === 'autotest') renderAutoTest();
  else if (name === 'competitive') renderCompetitive();
  else if (name === 'apidesign') renderAPIDesign();
  else if (name === 'apidoc') renderAPIDoc();
  else if (name === 'analytics') renderAnalytics();
  else if (name === 'settings') renderSettings();
  else if (name === 'ued') renderUED();
}

// ===== MODAL =====
function openModal(id) {
  var el = document.getElementById(id);
  if (el) el.classList.add('open');
}
function closeModal(id) {
  var el = document.getElementById(id);
  if (el) el.classList.remove('open');
}
// Close modal on background click
document.addEventListener('click', function(e) {
  if (e.target && e.target.classList && e.target.classList.contains('modal-bg')) {
    e.target.classList.remove('open');
  }
});

// ===== TAB SWITCHER =====
function switchTab(el, prefix) {
  var parent = el.parentElement;
  var tabs = parent.querySelectorAll('.tab');
  var idx = Array.from(tabs).indexOf(el);
  tabs.forEach(function(t) { t.classList.remove('active'); });
  el.classList.add('active');
  var panes = document.querySelectorAll('[id^="' + prefix + '-tp-"]');
  panes.forEach(function(p, i) { p.classList.toggle('active', i === idx); });
}

function switchTabByIdx(containerEl, idx) {
  var tabs = containerEl.querySelectorAll('.tab');
  tabs.forEach(function(t, i) { t.classList.toggle('active', i === idx); });
  // find panes - they follow the tabs container
  var parent = containerEl.parentElement || containerEl;
  var panes = parent.querySelectorAll('.tp');
  panes.forEach(function(p, i) { p.classList.toggle('active', i === idx); });
}

// ===== DASHBOARD =====
function renderDashboard() {
  var projEl = document.getElementById('dashProjects');
  if (projEl) {
    var html = '';
    state.projects.forEach(function(p) {
      var dot = '<div style="width:8px;height:8px;border-radius:50%;background:' + healthColor(p.health) + ';flex-shrink:0"></div>';
      var pbar = '<div class="pb" style="width:70px"><div class="pf ' + (p.health==='green'?'pfg':p.health==='amber'?'pfam':'pfrd') + '" style="width:' + p.progress + '%"></div></div>';
      html += '<div style="display:flex;align-items:center;gap:8px;padding:8px 0;border-bottom:1px solid var(--g100);cursor:pointer" onclick="showPage(\'projects\')">';
      html += dot + '<div style="flex:1;font-size:12.5px">' + p.name + '</div>' + pbar;
      html += '<span style="font-size:12px;font-weight:600;width:32px;text-align:right">' + p.progress + '%</span></div>';
    });
    projEl.innerHTML = html;
  }

  var todoEl = document.getElementById('dashTodos');
  if (todoEl) {
    var todos = [
      {emoji:'🔴',title:'审批：病虫害模块提测申请',sub:'测试团队 · 2小时前 · 紧急',color:'var(--rdl)',page:'submit'},
      {emoji:'🟡',title:'评审：智慧灌溉v2.1 PRD变更',sub:'产品团队 · 今天10:30 · AI已生成摘要',color:'var(--aml)',page:'prd'},
      {emoji:'🔵',title:'确认：溯源平台运维手册终审',sub:'AI已生成完整手册 · 待终审',color:'var(--bll)',page:'opsmanual'}
    ];
    var h = '';
    todos.forEach(function(t) {
      h += '<div style="display:flex;gap:10px;padding:9px;background:' + t.color + ';border-radius:8px;margin-bottom:8px;cursor:pointer" onclick="showPage(\'' + t.page + '\')">';
      h += '<span style="font-size:16px">' + t.emoji + '</span>';
      h += '<div><div style="font-weight:600;font-size:12.5px;color:var(--g800);margin-bottom:2px">' + t.title + '</div>';
      h += '<div style="font-size:11.5px;color:var(--g500)">' + t.sub + '</div></div></div>';
    });
    todoEl.innerHTML = h;
  }

  var qsEl = document.getElementById('qualitySnapshot');
  if (qsEl) {
    var qs = [
      {label:'测试覆盖率',val:'76%',color:'var(--bl)',w:76},
      {label:'自动化覆盖率',val:'62%',color:'var(--pu)',w:62},
      {label:'P0缺陷修复率',val:'100%',color:'#10b981',w:100},
      {label:'代码扫描通过率',val:'91%',color:'var(--gp)',w:91}
    ];
    var qh = '';
    qs.forEach(function(q) {
      qh += '<div style="margin-bottom:12px">';
      qh += '<div style="display:flex;justify-content:space-between;margin-bottom:4px;font-size:12px"><span>' + q.label + '</span>';
      qh += '<span style="font-weight:700;color:' + q.color + '">' + q.val + '</span></div>';
      qh += '<div class="pb" style="height:7px"><div class="pf" style="width:' + q.w + '%;background:' + q.color + '"></div></div></div>';
    });
    qsEl.innerHTML = qh;
  }
}

// ===== PROJECTS =====
function renderProjects() {
  var t = document.getElementById('projectsTable');
  if (!t) return;
  var h = '<thead><tr><th>项目名称</th><th>业务线</th><th>阶段</th><th>进度</th><th>健康度</th><th>负责人</th><th>截止日期</th><th>操作</th></tr></thead><tbody>';
  state.projects.forEach(function(p) {
    var statusC = {研发中:'bbl',测试中:'bam',验收中:'bg',规划中:'bgr'};
    h += '<tr>';
    h += '<td><strong>' + p.name + '</strong></td>';
    h += '<td>' + badge('bg', p.biz) + '</td>';
    h += '<td>' + badge(statusC[p.status]||'bgr', p.status) + '</td>';
    h += '<td><div style="display:flex;align-items:center;gap:8px"><div class="pb" style="width:70px"><div class="pf ' + (p.health==='green'?'pfg':p.health==='amber'?'pfam':'pfrd') + '" style="width:' + p.progress + '%"></div></div><span style="font-size:12px">' + p.progress + '%</span></div></td>';
    h += '<td>' + healthBadge(p.health) + '</td>';
    h += '<td>' + p.owner + '</td>';
    h += '<td style="font-size:11.5px">' + p.end + '</td>';
    h += '<td><button class="btn btn-s btn-sm" onclick="openProjectDetail(\'' + p.id + '\')">详情</button></td>';
    h += '</tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
  var cnt = document.getElementById('projCount');
  if (cnt) cnt.textContent = state.projects.length;
}

function filterProjects(val) { renderProjects(); }

function addProject() {
  var name = document.getElementById('np-name').value;
  if (!name) { notify('请输入项目名称', 'err'); return; }
  state.projects.push({
    id: 'P00' + (state.projects.length+1),
    name: name,
    biz: document.getElementById('np-biz').value,
    status: '规划中',
    progress: 0,
    health: 'green',
    owner: document.getElementById('np-owner').value || '待分配',
    end: document.getElementById('np-end').value
  });
  closeModal('modal-newproject');
  renderProjects();
  notify('项目"' + name + '"已创建', '');
}

// ===== REQUIREMENTS =====
function renderRequirements(filter) {
  var rows = filter ? state.requirements.filter(function(r) { return r.status === filter; }) : state.requirements;
  var t = document.getElementById('reqTable');
  if (!t) return;
  var h = '<thead><tr><th>ID</th><th>需求标题</th><th>来源</th><th>优先级</th><th>状态</th><th>AI评估</th><th>操作</th></tr></thead><tbody>';
  rows.forEach(function(r) {
    h += '<tr>';
    h += '<td style="font-size:11.5px;color:var(--g400)">' + r.id + '</td>';
    h += '<td style="max-width:240px">' + r.title + '</td>';
    h += '<td>' + badge('bbl', r.src) + '</td>';
    h += '<td>' + priLabel(r.pri) + '</td>';
    h += '<td>' + statusBadge(r.status) + '</td>';
    h += '<td>' + badge('bai', '🤖 ' + r.ai) + '</td>';
    h += '<td style="white-space:nowrap"><button class="btn btn-s btn-sm" onclick="openReqDetail(\'' + r.id + '\')">查看</button> <button class="btn btn-sm" style="padding:4px 7px;background:var(--rdl);color:var(--rd);border:1px solid #fecaca;border-radius:6px;cursor:pointer" onclick="quickDeleteReq(\'' + r.id + '\')">🗑️</button></td>';
    h += '</tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
}

function switchReqTab(el, idx) {
  document.querySelectorAll('#pg-requirements .tab').forEach(function(t) { t.classList.remove('active'); });
  el.classList.add('active');
  var filters = ['', '待评审', '开发中', '已完成'];
  renderRequirements(filters[idx]);
}

function addReq() {
  var title = document.getElementById('nr-title').value;
  if (!title) { notify('请输入需求标题', 'err'); return; }
  var priMap = {'P0 - 紧急':'P0','P1 - 重要':'P1','P2 - 一般':'P2'};
  var priVal = document.getElementById('nr-pri').value;
  state.requirements.unshift({
    id: 'REQ-' + (90 + state.requirements.length),
    title: title,
    src: document.getElementById('nr-src').value,
    pri: priMap[priVal] || 'P1',
    status: '待评审',
    ai: '待评估',
    desc: document.getElementById('nr-desc').value
  });
  closeModal('modal-newreq');
  renderRequirements();
  notify('需求"' + title + '"已创建', '');
}

function aiAnalyzeReq() {
  notify('AI正在分析需求价值和优先级…', 'info');
  setTimeout(function() {
    var vals = ['高价值','中价值','低价值'];
    state.requirements.forEach(function(r) { r.ai = vals[Math.floor(Math.random()*3)]; });
    renderRequirements();
    notify('AI优先级分析完成，基于业务价值、用户痛点和技术复杂度综合评分', '');
  }, 1800);
}

function aiEvalReq() {
  closeModal('modal-newreq');
  notify('AI评估：该需求业务价值中等，建议优先级P1，可关联已有需求REQ-088', 'info');
}

function quickDeleteReq(id) {
  var idx = state.requirements.findIndex(function(r) { return r.id === id; });
  if (idx > -1) { state.requirements.splice(idx, 1); renderRequirements(); notify('需求 ' + id + ' 已删除', 'warn'); }
}

// ===== KANBAN =====
var dragTaskId = null;

function renderKanban() {
  var cols = ['待开发','开发中','代码评审','测试中','已完成'];
  var colColors = {'待开发':'#6b7280','开发中':'var(--bl)','代码评审':'var(--pu)','测试中':'var(--am)','已完成':'#10b981'};
  var b = document.getElementById('kanbanBoard');
  if (!b) return;
  var html = '';
  cols.forEach(function(col) {
    var tasks = state.tasks.filter(function(t) { return t.col === col; });
    html += '<div class="kbc" data-col="' + col + '" ondragover="event.preventDefault();this.classList.add(\'drag-over\')" ondragleave="this.classList.remove(\'drag-over\')" ondrop="dropTask(event,\'' + col + '\')">';
    html += '<div class="kbch"><span style="color:' + colColors[col] + '">' + col + '</span><span class="kbcc">' + tasks.length + '</span></div>';
    tasks.forEach(function(t) {
      var border = col === '开发中' ? 'border-left:3px solid var(--bl)' : col === '代码评审' ? 'border-left:3px solid var(--pu)' : '';
      html += '<div class="kbcard" draggable="true" data-taskid="' + t.id + '" style="' + border + '" ondragstart="dragStart(event,\'' + t.id + '\')" ondragend="dragEnd(event)" onclick="openTaskDetail(\'' + t.id + '\')">';
      html += '<div class="kbct">' + t.title + '</div>';
      html += '<div class="kbcm"><span style="color:var(--g400);font-size:10.5px">' + t.id + '</span>' + priLabel(t.pri) + '</div>';
      html += '<div style="font-size:10.5px;color:var(--g500);margin-top:4px">👤 ' + t.owner + ' · ⏱️ ' + t.hours + 'h</div>';
      html += '</div>';
    });
    html += '<div style="border:1.5px dashed var(--g300);border-radius:7px;padding:7px;text-align:center;color:var(--g400);font-size:12px;cursor:pointer;margin-top:5px" onclick="openModal(\'modal-newtask\')">+ 添加任务</div>';
    html += '</div>';
  });
  b.innerHTML = html;
}

function dragStart(event, taskId) {
  dragTaskId = taskId;
  event.dataTransfer.effectAllowed = 'move';
  setTimeout(function() {
    var el = document.querySelector('[data-taskid="' + taskId + '"]');
    if (el) el.classList.add('dragging');
  }, 0);
}
function dragEnd() {
  document.querySelectorAll('.kbcard').forEach(function(c) { c.classList.remove('dragging'); });
  document.querySelectorAll('.kbc').forEach(function(c) { c.classList.remove('drag-over'); });
}
function dropTask(event, col) {
  event.preventDefault();
  event.currentTarget.classList.remove('drag-over');
  if (!dragTaskId) return;
  var task = state.tasks.find(function(t) { return t.id === dragTaskId; });
  if (task && task.col !== col) {
    task.col = col;
    renderKanban();
    notify('任务已移动到【' + col + '】', '');
    dragTaskId = null;
  }
}

function addTask() {
  var title = document.getElementById('nt-title').value;
  if (!title) { notify('请输入任务标题', 'err'); return; }
  state.tasks.unshift({
    id: 'TASK-' + (130 + state.tasks.length),
    title: title,
    col: document.getElementById('nt-col').value,
    pri: document.getElementById('nt-pri').value,
    owner: document.getElementById('nt-owner').value,
    hours: parseInt(document.getElementById('nt-hours').value) || 8,
    desc: ''
  });
  closeModal('modal-newtask');
  renderKanban();
  notify('任务"' + title + '"已创建', '');
}

function aiSplitTasks() {
  notify('AI正在根据PRD拆分研发任务…', 'info');
  setTimeout(function() {
    state.tasks.unshift({id:'TASK-127',title:'AI灌溉推荐接口开发',col:'待开发',pri:'P0',owner:'王工',hours:24,desc:''});
    state.tasks.unshift({id:'TASK-128',title:'Dify工作流灌溉模型集成',col:'待开发',pri:'P0',owner:'王工',hours:16,desc:''});
    renderKanban();
    notify('AI已新增2个任务：灌溉接口开发 + Dify集成', '');
  }, 1500);
}

// ===== TASK DETAIL =====
function openTaskDetail(taskId) {
  var task = state.tasks.find(function(t) { return t.id === taskId; });
  if (!task) return;
  state.editingTaskId = taskId;
  document.getElementById('tdm-title').textContent = task.title;
  document.getElementById('tdm-id').textContent = task.id + ' · ' + task.col;
  document.getElementById('tdm-edit-title').value = task.title;
  document.getElementById('tdm-edit-desc').value = task.desc || '';
  setSelectValue('tdm-col', task.col);
  setSelectValue('tdm-pri', task.pri);
  setSelectValue('tdm-owner', task.owner);
  document.getElementById('tdm-hours').value = task.hours;
  renderMRList(taskId);
  renderTaskComments(taskId);
  var hist = document.getElementById('tdm-history');
  if (hist) hist.innerHTML = '<div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">状态：' + task.col + '</div><div class="tls">' + task.owner + ' · 刚刚</div></div></div><div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">任务创建</div><div class="tls">系统 · 2026-04-10</div></div></div>';
  openModal('modal-taskdetail');
}
function renderMRList(taskId) {
  var mrs = state.taskMRs[taskId] || [];
  var el = document.getElementById('tdm-mr-list');
  if (!el) return;
  if (!mrs.length) { el.innerHTML = '<div style="font-size:12px;color:var(--g400);padding:8px 0">暂未关联MR</div>'; return; }
  var h = '';
  mrs.forEach(function(mr) {
    h += '<div class="mr-card"><div style="display:flex;justify-content:space-between"><span style="font-weight:600;font-size:12px">' + mr.title + '</span>' + badge(mr.status==='已合并'?'bg':'bam', mr.status) + '</div>';
    h += '<div style="font-size:11px;color:var(--g500);margin-top:2px">' + mr.branch + ' · ' + mr.author + '</div></div>';
  });
  el.innerHTML = h;
}
function renderTaskComments(taskId) {
  var comments = state.taskComments[taskId] || [];
  var el = document.getElementById('tdm-comments');
  if (!el) return;
  if (!comments.length) { el.innerHTML = '<div style="font-size:12px;color:var(--g400);padding:8px 0">暂无评论</div>'; return; }
  var h = '';
  comments.forEach(function(c) {
    h += '<div class="cmt-item"><div class="cmt-av" style="background:var(--gp)">' + c.author[0] + '</div>';
    h += '<div class="cmt-body"><div class="cmt-who">' + c.author + ' · ' + c.time + '</div>' + c.text + '</div></div>';
  });
  el.innerHTML = h;
}
function addTaskComment() {
  var input = document.getElementById('tdm-comment-input');
  var text = input ? input.value.trim() : '';
  if (!text || !state.editingTaskId) return;
  if (!state.taskComments[state.editingTaskId]) state.taskComments[state.editingTaskId] = [];
  state.taskComments[state.editingTaskId].push({author:'张总', text:text, time:'刚刚'});
  input.value = '';
  renderTaskComments(state.editingTaskId);
}
function linkNewMR() {
  var taskId = state.editingTaskId;
  if (!taskId) return;
  if (!state.taskMRs[taskId]) state.taskMRs[taskId] = [];
  var task = state.tasks.find(function(t) { return t.id === taskId; });
  var branch = 'feature/' + taskId.toLowerCase() + '-impl';
  state.taskMRs[taskId].push({title:'[' + taskId + '] ' + (task ? task.title.slice(0,20) : '功能'), branch:branch, author:'王工', status:'评审中'});
  renderMRList(taskId);
  notify('已关联MR: ' + branch, '');
}
function saveTaskDetail() {
  var task = state.tasks.find(function(t) { return t.id === state.editingTaskId; });
  if (!task) return;
  task.title = document.getElementById('tdm-edit-title').value || task.title;
  task.desc = document.getElementById('tdm-edit-desc').value;
  task.col = document.getElementById('tdm-col').value;
  task.pri = document.getElementById('tdm-pri').value;
  task.owner = document.getElementById('tdm-owner').value;
  task.hours = parseInt(document.getElementById('tdm-hours').value) || task.hours;
  closeModal('modal-taskdetail');
  renderKanban();
  notify('任务已保存', '');
}
function deleteTask() {
  var idx = state.tasks.findIndex(function(t) { return t.id === state.editingTaskId; });
  if (idx > -1) state.tasks.splice(idx, 1);
  closeModal('modal-taskdetail');
  renderKanban();
  notify('任务已删除', 'warn');
}

// ===== PROJECT DETAIL =====
function openProjectDetail(projId) {
  var p = state.projects.find(function(x) { return x.id === projId; });
  if (!p) return;
  document.getElementById('pdm-title').textContent = p.name;
  document.getElementById('pdm-meta').textContent = p.biz + ' · ' + p.status + ' · 负责人：' + p.owner + ' · 截止：' + p.end;
  // Reset to tab 0
  var modal = document.getElementById('modal-projectdetail');
  modal.querySelectorAll('.tab').forEach(function(t,i) { t.classList.toggle('active', i===0); });
  modal.querySelectorAll('.tp').forEach(function(p,i) { p.classList.toggle('active', i===0); });
  renderProjectOverview(p);
  renderProjectMilestones(p);
  renderProjectGantt();
  renderProjectTeam();
  openModal('modal-projectdetail');
}
function switchPDTab(el, idx) {
  var modal = el.closest('.modal');
  modal.querySelectorAll('.tab').forEach(function(t,i) { t.classList.toggle('active', i===idx); });
  modal.querySelectorAll('.tp').forEach(function(p,i) { p.classList.toggle('active', i===idx); });
}
function renderProjectOverview(p) {
  var el = document.getElementById('pdm-overview');
  if (!el) return;
  var h = '<div class="g2 mb3">';
  h += '<div class="sc"><div class="sl">项目进度</div><div class="sv" style="color:' + healthColor(p.health) + '">' + p.progress + '%</div></div>';
  h += '<div class="sc"><div class="sl">健康状态</div><div class="sv" style="font-size:16px">' + (p.health==='green'?'🟢 健康':p.health==='amber'?'🟡 注意':'🔴 风险') + '</div></div>';
  h += '</div>';
  h += progressBar(p.progress, p.health==='green'?'pfg':p.health==='amber'?'pfam':'pfrd');
  h += '<div style="font-size:12.5px;font-weight:700;margin:12px 0 8px">本迭代任务分布</div>';
  var cols = ['待开发','开发中','代码评审','测试中','已完成'];
  cols.forEach(function(col) {
    var cnt = state.tasks.filter(function(t) { return t.col === col; }).length;
    var pct = state.tasks.length ? (cnt/state.tasks.length*100) : 0;
    h += '<div style="display:flex;align-items:center;gap:8px;margin-bottom:5px"><span style="width:70px;font-size:12px">' + col + '</span>';
    h += '<div class="pb" style="flex:1"><div class="pf pfbl" style="width:' + pct + '%"></div></div>';
    h += '<span style="font-size:12px;width:20px">' + cnt + '</span></div>';
  });
  el.innerHTML = h;
}
function renderProjectMilestones(p) {
  var el = document.getElementById('pdm-milestones');
  if (!el) return;
  var milestones = [
    {name:'需求评审完成',date:'2026-03-05',done:true},
    {name:'系统设计完成',date:'2026-03-20',done:true},
    {name:'核心功能开发完成',date:'2026-04-30',done:false},
    {name:'系统测试完成',date:'2026-05-15',done:false},
    {name:'验收上线',date:'2026-05-20',done:false}
  ];
  var h = '<div class="tl">';
  milestones.forEach(function(m) {
    h += '<div class="tli"><div class="tld ' + (m.done?'done':'') + '"></div>';
    h += '<div class="tlc"><div class="tlt">' + (m.done?'✅ ':'') + m.name + '</div><div class="tls">' + m.date + '</div></div></div>';
  });
  h += '</div>';
  el.innerHTML = h;
}
function renderProjectGantt() {
  var el = document.getElementById('pdm-gantt');
  if (!el) return;
  var tasks = [
    {name:'基础架构搭建',start:0,len:10,color:'#10b981'},
    {name:'传感器数据接入',start:8,len:12,color:'var(--bl)'},
    {name:'灌溉推荐AI引擎',start:18,len:20,color:'var(--pu)'},
    {name:'移动端页面开发',start:22,len:18,color:'var(--am)'},
    {name:'离线同步功能',start:30,len:14,color:'var(--gp)'},
    {name:'集成测试',start:42,len:10,color:'var(--rd)'}
  ];
  var total = 55, todayOffset = 35;
  var h = '<div style="overflow-x:auto"><div style="min-width:500px">';
  h += '<div style="display:flex;border-bottom:2px solid var(--g200);padding-bottom:4px;margin-bottom:4px">';
  h += '<div style="width:155px;font-size:11px;color:var(--g500);font-weight:700">任务名称</div>';
  h += '<div style="flex:1;display:flex;font-size:10px;color:var(--g400)">';
  for (var w = 1; w <= 8; w++) {
    h += '<div style="flex:1;text-align:center">第' + (w*7) + '天</div>';
  }
  h += '</div></div>';
  tasks.forEach(function(t) {
    var leftPct = (t.start/total*100);
    var widthPct = (t.len/total*100);
    var todayPct = (todayOffset/total*100);
    h += '<div style="display:flex;align-items:center;padding:3px 0;border-bottom:1px solid var(--g100)">';
    h += '<div style="width:155px;font-size:12px;padding-right:8px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis">' + t.name + '</div>';
    h += '<div style="flex:1;height:22px;position:relative;background:var(--g50);border-radius:3px">';
    h += '<div style="position:absolute;width:2px;background:rgba(239,68,68,.6);top:0;bottom:0;left:' + todayPct + '%"></div>';
    h += '<div style="position:absolute;height:18px;top:2px;left:' + leftPct + '%;width:' + widthPct + '%;background:' + t.color + ';border-radius:4px;display:flex;align-items:center;padding:0 6px;font-size:10px;color:#fff;font-weight:600;overflow:hidden;white-space:nowrap">' + t.name + '</div>';
    h += '</div></div>';
  });
  h += '<div style="font-size:11px;color:var(--g400);margin-top:6px">红线 = 今日进度线（第35天）</div>';
  h += '</div></div>';
  el.innerHTML = h;
}
function renderProjectTeam() {
  var el = document.getElementById('pdm-team');
  if (!el) return;
  var members = [
    {name:'王工',role:'后端开发',tasks:4,color:'var(--bl)'},
    {name:'李工',role:'前端开发',tasks:3,color:'var(--pu)'},
    {name:'陈工',role:'算法工程师',tasks:2,color:'#10b981'},
    {name:'赵工',role:'测试工程师',tasks:5,color:'var(--am)'}
  ];
  var h = '';
  members.forEach(function(m) {
    h += '<div style="display:flex;align-items:center;gap:12px;padding:10px;background:var(--g50);border-radius:8px;margin-bottom:7px">';
    h += '<div style="width:36px;height:36px;border-radius:50%;background:' + m.color + ';display:flex;align-items:center;justify-content:center;color:#fff;font-weight:700;font-size:14px;flex-shrink:0">' + m.name[0] + '</div>';
    h += '<div style="flex:1"><div style="font-weight:600;font-size:13px">' + m.name + '</div><div style="font-size:11.5px;color:var(--g500)">' + m.role + '</div></div>';
    h += '<div style="font-size:12px;font-weight:600;color:var(--gp)">' + m.tasks + ' 个任务</div></div>';
  });
  el.innerHTML = h;
}
function addMilestone() { notify('里程碑已添加到计划中', ''); }

// ===== INCEPTION =====
function runInceptionAI() {
  var name = document.getElementById('inc-name') ? document.getElementById('inc-name').value : '新项目';
  var report = document.getElementById('incReport');
  var actions = document.getElementById('incActions');
  var riskCard = document.getElementById('incRiskCard');
  if (report) report.innerHTML = '<div style="text-align:center;padding:20px"><div class="ai-gen" style="justify-content:center">🤖 AI分析中 <span class="dots"><span></span><span></span><span></span></span></div></div>';
  notify('AgriAI正在生成立项建议书…', 'info');
  setTimeout(function() {
    if (report) {
      report.innerHTML = '<div class="prd"><h3>📋 立项建议书 · ' + name + '</h3>' +
        '<h4>一、项目背景</h4><p>农业病虫害每年造成全国农作物减产约<strong>20-30%</strong>，经济损失超千亿元。现有人工识别方式依赖专家经验，响应时间长，专家资源严重匮乏。</p>' +
        '<h4>二、市场机会</h4><p>全国植保服务市场规模约<strong>580亿元</strong>（2025年），数字化渗透率不足8%，市场空间巨大。AI视觉识别技术已达商用级别，准确率可达95%+。</p>' +
        '<h4>三、ROI预估</h4><ul><li>开发成本：约 <strong>180万元</strong>（6个月，10人团队）</li><li>目标付费用户：首年1万家农场，客单价3000元/年</li><li>预计首年营收：<strong>3000万元</strong>，ROI达 <strong>16.7倍</strong></li></ul>' +
        '<h4>四、建议决策</h4><p style="font-weight:700;color:var(--gp)">✅ 建议立项，优先级P1，计划Q3启动，分3期交付。</p></div>';
    }
    if (actions) actions.style.display = 'block';
    if (riskCard) {
      riskCard.style.display = 'block';
      var risks = document.getElementById('incRisks');
      if (risks) {
        risks.innerHTML = '<div style="display:flex;flex-direction:column;gap:8px">' +
          '<div style="padding:9px;background:var(--aml);border-radius:7px;border-left:3px solid var(--am);font-size:12.5px"><strong>⚠️ 数据集风险</strong><br><span style="color:var(--g600)">病虫害图像训练数据集可能不足，需提前采购或与农科院合作。</span></div>' +
          '<div style="padding:9px;background:var(--rdl);border-radius:7px;border-left:3px solid var(--rd);font-size:12.5px"><strong>🔴 监管合规风险</strong><br><span style="color:var(--g600)">农药推荐功能需取得相关资质，建议提前咨询法务。</span></div>' +
          '</div>';
      }
    }
    notify('立项建议书已生成，共识别2项风险', '');
  }, 2000);
}

function incApprove() {
  notify('立项申请已提交审批，将通过飞书推送给审批人', 'info');
  setTimeout(function() { showPage('competitive'); }, 500);
}

// ===== COMPETITIVE =====
function renderCompetitive() {
  var dims = ['立项管理','AI竞品分析','AI PRD生成','UED协同','AI设计文档','任务看板','AI编码辅助','AI测试用例','测试数据工厂','自动化测试','AI文档生成','MCP集成','Dify编排','农业知识库','私有化部署'];
  var headers = ['禅道', 'LigaAI', 'Jira+Rovo', 'Copilot WS', '本品★'];
  var data = [
    [1,0,0.5,0,1],[1,0,0.5,0,1],[1,0.5,0.5,0,1],[0,0,0.5,0,1],[0,0,0.5,0,1],
    [1,1,1,0,1],[0.5,0,1,1,1],[0.5,0.5,0,0,1],[0.5,0,0,0,1],[0.5,0,0.5,0,1],
    [0,0,0,0,1],[0,0,1,1,1],[0,0,0,0,1],[0,0,0,0,1],[1,0,0.5,0,1]
  ];
  var t = document.getElementById('compMatrix');
  if (t) {
    var h = '<thead><tr><th>功能维度</th>';
    headers.forEach(function(hd, ci) {
      h += '<th style="text-align:center">' + (ci===4?'<span style="color:var(--gp);font-weight:700">'+hd+'</span>':hd) + '</th>';
    });
    h += '</tr></thead><tbody>';
    dims.forEach(function(d, i) {
      h += '<tr><td style="font-weight:500;font-size:12.5px">' + d + '</td>';
      data[i].forEach(function(v, ci) {
        var icon = v===1 ? (ci===4?'★':'✓') : v===0.5 ? '△' : '✗';
        var bg = v===1 ? (ci===4?'#ede9fe':'#dcfce7') : v===0.5 ? '#fef3c7' : '#fee2e2';
        var color = v===1 ? (ci===4?'#5b21b6':'#166534') : v===0.5 ? '#92400e' : '#991b1b';
        h += '<td style="text-align:center"><span style="background:' + bg + ';color:' + color + ';padding:2px 10px;border-radius:6px;font-size:13px;font-weight:700">' + icon + '</span></td>';
      });
      h += '</tr>';
    });
    h += '</tbody>';
    t.innerHTML = h;
  }
  var m = document.getElementById('compMonitor');
  if (m) {
    m.innerHTML = '<thead><tr><th>竞品</th><th>最新动态</th><th>威胁度</th><th>日期</th></tr></thead><tbody>' +
      '<tr><td>禅道</td><td>发布AI助手Beta，支持代码评审辅助</td><td>' + badge('bam','中') + '</td><td>2026-04-10</td></tr>' +
      '<tr><td>LigaAI</td><td>新增测试用例AI生成功能</td><td>' + badge('bam','中') + '</td><td>2026-04-08</td></tr>' +
      '<tr><td>PingCode</td><td>集成通义千问，推出AI需求分析模块</td><td>' + badge('brd','高') + '</td><td>2026-04-12</td></tr>' +
      '<tr><td>Jira+Rovo</td><td>Rovo Dev新增更多MCP工具支持</td><td>' + badge('bbl','低') + '</td><td>2026-04-05</td></tr>' +
      '</tbody>';
  }
  var sw = document.getElementById('swotContent');
  if (sw) {
    sw.innerHTML = '<div class="g2">' +
      '<div style="background:#dcfce7;border-radius:8px;padding:12px"><div style="font-weight:700;font-size:13px;color:#166534;margin-bottom:8px">💪 优势</div><ul style="font-size:12px;padding-left:16px;line-height:1.8"><li>唯一覆盖农业全生命周期的平台</li><li>Dify编排深度集成</li><li>AgriKB农业专业知识库</li><li>MCP+CLI生态完整</li></ul></div>' +
      '<div style="background:#fee2e2;border-radius:8px;padding:12px"><div style="font-weight:700;font-size:13px;color:#991b1b;margin-bottom:8px">⚠️ 劣势</div><ul style="font-size:12px;padding-left:16px;line-height:1.8"><li>品牌知名度较低</li><li>销售渠道有限</li><li>初期功能有待完善</li></ul></div>' +
      '<div style="background:#dbeafe;border-radius:8px;padding:12px"><div style="font-weight:700;font-size:13px;color:#1d4ed8;margin-bottom:8px">🌱 机会</div><ul style="font-size:12px;padding-left:16px;line-height:1.8"><li>农业数字化转型加速</li><li>AI技术降低门槛</li><li>国产化替代需求旺盛</li></ul></div>' +
      '<div style="background:#fef3c7;border-radius:8px;padding:12px"><div style="font-weight:700;font-size:13px;color:#92400e;margin-bottom:8px">🔥 威胁</div><ul style="font-size:12px;padding-left:16px;line-height:1.8"><li>通用竞品快速AI化</li><li>大厂可能切入垂直赛道</li><li>农业客户付费意愿有限</li></ul></div>' +
      '</div>';
  }
}
function runCompAnalysis() {
  notify('AI正在爬取竞品信息并生成分析报告…', 'info');
  setTimeout(function() { notify('竞品分析报告已更新，识别到1个高威胁竞品动态', 'warn'); }, 2500);
}

// ===== PRD =====
function generatePRD() {
  var title = document.getElementById('prd-title') ? document.getElementById('prd-title').value : '新功能';
  var pc = document.getElementById('prdProgressCard');
  var content = document.getElementById('prdContent');
  var actionRow = document.getElementById('prdActionRow');
  var comp = document.getElementById('prdCompleteness');
  if (pc) pc.style.display = 'block';
  if (content) content.innerHTML = '<div style="text-align:center;padding:30px"><div class="ai-gen" style="justify-content:center;font-size:14px">🤖 AI生成中 <span class="dots"><span></span><span></span><span></span></span></div></div>';
  notify('AI开始生成PRD，调用Dify prd-generation-flow…', 'info');
  var steps = [
    {title:'分析需求背景与业务上下文', desc:'引用AgriKB灌溉知识库 23条', done:true},
    {title:'生成用户故事与功能描述', desc:'Dify flow: prd-generation-flow', done:true},
    {title:'生成非功能需求与验收标准', desc:'完成', done:true},
    {title:'输出结构化PRD文档', desc:'完成', done:true}
  ];
  function renderSteps(doneCount) {
    var prog = document.getElementById('prdProgress');
    if (!prog) return;
    var h = '';
    steps.forEach(function(s, i) {
      var dotCls = i < doneCount ? 'done' : i === doneCount ? 'ai' : '';
      var prefix = i < doneCount ? '✅ ' : (i === doneCount ? '<span class="ai-gen" style="font-size:12px">生成中 <span class="dots"><span></span><span></span><span></span></span></span> ' : '');
      h += '<div class="tli"><div class="tld ' + dotCls + '"></div><div class="tlc"><div class="tlt">' + prefix + s.title + '</div><div class="tls">' + s.desc + '</div></div></div>';
    });
    prog.innerHTML = h;
  }
  renderSteps(1);
  setTimeout(function() { renderSteps(2); }, 1500);
  setTimeout(function() { renderSteps(3); }, 3000);
  setTimeout(function() {
    renderSteps(4);
    if (content) {
      content.innerHTML = '<div class="prd">' +
        '<h3>📄 ' + title + ' PRD · v1.0</h3>' +
        '<div style="display:flex;gap:6px;margin-bottom:12px">' + badge('bai','🤖 AI生成') + badge('bg','完整度 89%') + '</div>' +
        '<h4>一、背景与目标</h4><p>基于土壤墒情传感器（TDR/FDR）、气象数据（温湿度、蒸散量ET₀）和作物生长模型（Kc系数），为农场主提供智能灌溉决策建议，目标将灌溉用水量降低<strong>20%</strong>，支持离线弱网环境。</p>' +
        '<h4>二、用户故事</h4><ul><li>作为种植户，我希望每天收到灌溉推荐推送</li><li>作为农场主，我希望弱网时仍能查看历史灌溉记录</li><li>作为农技人员，我希望自定义不同作物的灌溉阈值参数</li></ul>' +
        '<h4>三、核心功能</h4><ul><li><strong>F1 灌溉推荐引擎</strong>：调用Dify AI工作流，根据土壤含水率、ET₀蒸散量、作物Kc系数计算最优灌溉方案</li><li><strong>F2 离线模式</strong>：本地SQLite缓存最近7天数据，网络恢复后自动增量同步</li><li><strong>F3 一键执行</strong>：手机端一键下发灌溉指令，支持定时和即时两种模式</li></ul>' +
        '<h4>四、验收标准</h4><ul><li>灌溉推荐准确率经专家评测 ≥85%</li><li>离线数据同步成功率 ≥99.9%</li><li>API响应时间P99 ≤2秒</li></ul>' +
        '</div>';
    }
    if (comp) { comp.className = 'b bg'; comp.textContent = '完整度 89%'; }
    if (actionRow) actionRow.style.display = 'block';
    state.prdGenerated = true;
    notify('PRD已生成完成，完整度89%', '');
  }, 4500);
}

function savePRD() {
  var titleEl = document.getElementById('prd-title');
  var saveTitle = document.getElementById('prd-save-title');
  if (saveTitle && titleEl) saveTitle.value = titleEl.value;
  openModal('modal-prdsave');
}
function confirmPRDSave() {
  var title = document.getElementById('prd-save-title').value || 'PRD文档';
  var ver = document.getElementById('prd-save-ver').value || 'v1.0';
  var status = document.getElementById('prd-save-status').value;
  state.savedPRDs.push({title:title, ver:ver, status:status, date:dateStr()});
  closeModal('modal-prdsave');
  notify('PRD"' + title + '"已保存到文档库，版本 ' + ver, '');
}

// ===== ARCH DESIGN =====
function genArchDesign() {
  var mode = getVal('arch-mode') || '微服务架构';
  var lang = getVal('arch-lang') || 'Java(SpringBoot3)';
  var db = getVal('arch-db') || 'PostgreSQL+Redis';
  var aiEngine = getVal('arch-ai') || 'Dify+DeepSeek-V3';
  var deploy = getVal('arch-deploy') || 'Kubernetes';
  var iot = getVal('arch-iot') || 'MQTT(EMQ X)';
  notify('AI正在生成' + mode + '架构方案…', 'info');
  setTimeout(function() {
    var archEl = document.getElementById('archContent');
    if (archEl) {
      archEl.innerHTML = '<div style="margin-bottom:8px;display:flex;gap:7px;flex-wrap:wrap">' + badge('bai','🤖 AI生成') + badge('bg',mode) + badge('bbl',lang) + badge('bam',deploy) + '</div>' +
        '<div class="tl">' +
        '<div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">✅ 架构模式：' + mode + '</div><div class="tls">适合当前团队规模，各服务独立部署，AI推理服务可单独扩容GPU节点</div></div></div>' +
        '<div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">✅ 技术选型确认</div><div class="tls">后端：' + lang + ' / AI编排：' + aiEngine + ' / 数据库：' + db + ' / 向量库：Milvus</div></div></div>' +
        '<div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">✅ IoT接入层</div><div class="tls">' + iot + ' 协议接入传感器/气象站/灌溉控制器，支持10万设备并发连接</div></div></div>' +
        '<div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">✅ 部署方案：' + deploy + '</div><div class="tls">核心服务多副本部署，数据库主从架构，Redis集群模式</div></div></div>' +
        '</div>' +
        '<div class="btn-row mt3"><button class="btn btn-ai btn-sm" onclick="showPage(\'dbdesign\')">→ 进入数据库设计</button></div>';
    }
    var diagEl = document.getElementById('archDiagram');
    if (diagEl) {
      diagEl.innerHTML = '<div style="background:var(--g50);border-radius:8px;padding:12px;font-size:12px">' +
        '<div style="text-align:center;margin-bottom:10px;font-weight:700;color:var(--g600)">C4 容器图 · 智慧灌溉平台（' + mode + '）</div>' +
        '<div style="display:flex;flex-direction:column;gap:6px;align-items:center">' +
        '<div style="display:flex;gap:8px"><div style="background:var(--bll);border:2px solid var(--bl);border-radius:7px;padding:8px 12px;font-size:11.5px;font-weight:600;color:#1d4ed8">📱 小程序前端</div><div style="background:var(--bll);border:2px solid var(--bl);border-radius:7px;padding:8px 12px;font-size:11.5px;font-weight:600;color:#1d4ed8">🖥️ 管理后台</div></div>' +
        '<div style="color:var(--g400)">↓</div>' +
        '<div style="background:var(--gpale);border:2px solid var(--gl);border-radius:7px;padding:8px 18px;font-size:11.5px;font-weight:600;color:var(--gd)">🌐 API网关 (Kong)</div>' +
        '<div style="color:var(--g400)">↓</div>' +
        '<div style="display:flex;gap:6px"><div style="background:var(--pul);border:2px solid var(--pu);border-radius:7px;padding:7px 10px;font-size:11px;font-weight:600;color:var(--pu)">🤖 AI服务</div><div style="background:var(--gpale);border:2px solid var(--gl);border-radius:7px;padding:7px 10px;font-size:11px;font-weight:600;color:var(--gd)">💧 灌溉服务</div><div style="background:var(--gpale);border:2px solid var(--gl);border-radius:7px;padding:7px 10px;font-size:11px;font-weight:600;color:var(--gd)">📡 IoT服务</div></div>' +
        '<div style="color:var(--g400)">↓</div>' +
        '<div style="display:flex;gap:6px"><div style="background:var(--aml);border:2px solid var(--am);border-radius:7px;padding:7px 10px;font-size:11px;font-weight:600;color:#92400e">🗄️ ' + db.split('+')[0] + '</div><div style="background:var(--aml);border:2px solid var(--am);border-radius:7px;padding:7px 10px;font-size:11px;font-weight:600;color:#92400e">⚡ Redis</div><div style="background:var(--aml);border:2px solid var(--am);border-radius:7px;padding:7px 10px;font-size:11px;font-weight:600;color:#92400e">🔍 Milvus</div></div>' +
        '</div></div>';
    }
    var nfr = document.getElementById('archNFR');
    if (nfr) {
      nfr.style.display = 'block';
      var nfrEl = document.getElementById('archNFRContent');
      if (nfrEl) nfrEl.innerHTML = '<div class="g2"><div style="background:var(--g50);border-radius:8px;padding:12px;margin-bottom:10px;border-left:3px solid var(--gl)"><strong>性能</strong>：API P99 &lt;200ms，AI推荐 &lt;2s，IoT并发 10万设备</div><div style="background:var(--g50);border-radius:8px;padding:12px;margin-bottom:10px;border-left:3px solid var(--gl)"><strong>可用性</strong>：SLA 99.9%，多活部署，核心服务故障自动切换</div><div style="background:var(--g50);border-radius:8px;padding:12px;margin-bottom:10px;border-left:3px solid var(--gl)"><strong>安全</strong>：TLS1.3传输加密，RBAC权限，操作审计日志</div><div style="background:var(--g50);border-radius:8px;padding:12px;margin-bottom:10px;border-left:3px solid var(--gl)"><strong>扩展性</strong>：微服务独立扩缩容，支持5年内业务10倍增长</div></div>';
    }
    state.archGenerated = true;
    notify('系统架构方案已生成', '');
  }, 2000);
}

// ===== DB DESIGN =====
function genDBDesign() {
  notify('AI正在根据业务实体生成ER图和数据字典…', 'info');
  setTimeout(function() {
    var erEl = document.getElementById('erDiagram');
    if (erEl) {
      erEl.innerHTML = '<div style="background:var(--g50);border-radius:8px;padding:12px;font-size:12px">' +
        '<div style="text-align:center;font-weight:700;color:var(--g600);margin-bottom:10px">ER实体关系图</div>' +
        '<div style="display:flex;flex-wrap:wrap;gap:8px;justify-content:center">' +
        '<div style="background:#dbeafe;border:2px solid var(--bl);border-radius:7px;padding:8px 12px;text-align:center"><div style="font-weight:700;color:var(--bl);font-size:12px">t_field（地块）</div><div style="font-size:10.5px;color:var(--g600);margin-top:4px">field_id, code, name<br>area, gps_polygon, owner_id</div></div>' +
        '<div style="display:flex;align-items:center;font-size:18px;color:var(--g400)">⇒</div>' +
        '<div style="background:var(--gpale);border:2px solid var(--gl);border-radius:7px;padding:8px 12px;text-align:center"><div style="font-weight:700;color:var(--gd);font-size:12px">t_irrigation_plan</div><div style="font-size:10.5px;color:var(--g600);margin-top:4px">plan_id, field_id<br>recommend_time, water_amount</div></div>' +
        '</div><div style="display:flex;justify-content:center;margin:6px 0;font-size:12px;color:var(--g400)">⇓（关联传感器数据）</div>' +
        '<div style="display:flex;flex-wrap:wrap;gap:8px;justify-content:center">' +
        '<div style="background:#fef3c7;border:2px solid var(--am);border-radius:7px;padding:8px 12px;text-align:center"><div style="font-weight:700;color:#92400e;font-size:12px">t_soil_sensor_data</div><div style="font-size:10.5px;color:var(--g600);margin-top:4px">sensor_id, field_id<br>moisture_pct, temperature</div></div>' +
        '<div style="background:#fce7f3;border:2px solid #ec4899;border-radius:7px;padding:8px 12px;text-align:center"><div style="font-weight:700;color:#be185d;font-size:12px">t_weather_record</div><div style="font-size:10.5px;color:var(--g600);margin-top:4px">station_id, temperature<br>et0, rainfall, record_date</div></div>' +
        '</div></div>';
    }
    var dictEl = document.getElementById('dbDict');
    if (dictEl) {
      dictEl.innerHTML = '<table class="tbl"><thead><tr><th>字段名</th><th>类型</th><th>说明</th><th>约束</th></tr></thead><tbody>' +
        '<tr><td style="font-family:monospace;font-size:11.5px">field_id</td><td>VARCHAR(32)</td><td>地块唯一标识</td><td>PK, NOT NULL</td></tr>' +
        '<tr><td style="font-family:monospace;font-size:11.5px">moisture_pct</td><td>DECIMAL(5,2)</td><td>土壤含水率(%)</td><td>CHECK(0-100)</td></tr>' +
        '<tr><td style="font-family:monospace;font-size:11.5px">recommend_time</td><td>TIME</td><td>推荐灌溉时间</td><td>NOT NULL</td></tr>' +
        '<tr><td style="font-family:monospace;font-size:11.5px">confidence</td><td>DECIMAL(3,2)</td><td>AI推荐置信度</td><td>CHECK(0-1)</td></tr>' +
        '</tbody></table>';
    }
    var sqlCard = document.getElementById('dbSqlCard');
    if (sqlCard) {
      sqlCard.style.display = 'block';
      var sqlEl = document.getElementById('dbSql');
      if (sqlEl) {
        sqlEl.innerHTML = '<span style="color:#6b7280">-- AI生成 · 智慧灌溉平台核心表结构</span>\n' +
          '<span style="color:#93c5fd">CREATE TABLE</span> t_field (\n' +
          '  field_id    <span style="color:#fb923c">VARCHAR</span>(32) <span style="color:#93c5fd">PRIMARY KEY</span>,\n' +
          '  field_code  <span style="color:#fb923c">VARCHAR</span>(20) <span style="color:#93c5fd">UNIQUE NOT NULL</span>,\n' +
          '  area_mu     <span style="color:#fb923c">DECIMAL</span>(10,2),\n' +
          '  owner_id    <span style="color:#fb923c">BIGINT</span>,\n' +
          '  created_at  <span style="color:#fb923c">TIMESTAMP</span> <span style="color:#93c5fd">DEFAULT</span> NOW()\n);\n\n' +
          '<span style="color:#93c5fd">CREATE TABLE</span> t_irrigation_plan (\n' +
          '  plan_id         <span style="color:#fb923c">BIGSERIAL</span> <span style="color:#93c5fd">PRIMARY KEY</span>,\n' +
          '  field_id        <span style="color:#fb923c">VARCHAR</span>(32) <span style="color:#93c5fd">REFERENCES</span> t_field(field_id),\n' +
          '  recommend_time  <span style="color:#fb923c">TIME</span>,\n' +
          '  water_amount    <span style="color:#fb923c">DECIMAL</span>(8,2),\n' +
          '  confidence      <span style="color:#fb923c">DECIMAL</span>(3,2),\n' +
          '  status          <span style="color:#fb923c">VARCHAR</span>(20) <span style="color:#93c5fd">DEFAULT</span> <span style="color:#34d399">\'pending\'</span>\n);';
      }
    }
    state.dbGenerated = true;
    notify('数据库设计已生成，包含ER图、数据字典、建表SQL', '');
  }, 2000);
}

function copySQL() {
  var el = document.getElementById('dbSql');
  if (!el) return;
  copyToClipboard(el.innerText, 'SQL已复制到剪贴板');
}

// ===== API DESIGN =====
function renderAPIDesign() {
  var t = document.getElementById('apiDesignTable');
  if (!t) return;
  var h = '<thead><tr><th>ID</th><th>方法</th><th>路径</th><th>描述</th><th>状态</th><th>操作</th></tr></thead><tbody>';
  state.apiDesigns.forEach(function(a) {
    h += '<tr style="cursor:pointer" onclick="showAPIDetail(\'' + a.id + '\')">';
    h += '<td style="font-size:11px;color:var(--g400)">' + a.id + '</td>';
    h += '<td>' + methodBadge(a.method) + '</td>';
    h += '<td style="font-family:monospace;font-size:12px">' + a.path + '</td>';
    h += '<td>' + a.desc + '</td>';
    h += '<td>' + badge(a.status==='已确认'?'bg':'bam', a.status) + '</td>';
    h += '<td><button class="btn btn-s btn-sm" onclick="event.stopPropagation();showAPIDetail(\'' + a.id + '\')">详情</button></td>';
    h += '</tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
}

function showAPIDetail(id) {
  var a = state.apiDesigns.find(function(x) { return x.id === id; });
  if (!a) return;
  var params = id === 'API-001' ?
    '<tr><td>field_code</td><td>string</td><td>是</td><td>地块编号</td></tr><tr><td>crop_type</td><td>string</td><td>否</td><td>作物类型</td></tr>' :
    id === 'API-002' ?
    '<tr><td>sensor_id</td><td>string</td><td>是</td><td>传感器ID</td></tr><tr><td>data</td><td>object</td><td>是</td><td>传感器数据</td></tr>' :
    '<tr><td>id</td><td>string</td><td>是</td><td>资源ID</td></tr>';
  var el = document.getElementById('apiDetailView');
  if (!el) return;
  el.innerHTML = '<div style="margin-bottom:12px">' + methodBadge(a.method) + ' <code style="font-size:12.5px;background:var(--g100);padding:3px 8px;border-radius:5px">' + a.path + '</code></div>' +
    '<div style="font-size:12.5px;color:var(--g600);margin-bottom:14px">' + a.desc + '</div>' +
    '<div style="font-size:12px;font-weight:700;margin-bottom:6px">请求参数</div>' +
    '<table class="tbl" style="margin-bottom:12px"><thead><tr><th>参数名</th><th>类型</th><th>必填</th><th>说明</th></tr></thead><tbody>' + params + '</tbody></table>' +
    '<div style="font-size:12px;font-weight:700;margin-bottom:6px">响应示例</div>' +
    '<div class="code" style="font-size:11px">{"code":200,"msg":"success","data":{"recommend_time":"06:30","water_amount":12.5,"confidence":0.92}}</div>' +
    '<div class="btn-row mt3">' +
    '<button class="btn btn-ai btn-sm" onclick="openMockServer(\'' + a.path + '\')">🔧 启动Mock</button>' +
    '<button class="btn btn-s btn-sm" onclick="showPage(\'apidoc\')">📗 API文档</button>' +
    '</div>';
}

function genAPIDesign() {
  notify('AI正在根据功能模块生成OpenAPI接口设计…', 'info');
  setTimeout(function() {
    renderAPIDesign();
    notify('已生成 ' + state.apiDesigns.length + ' 个接口设计，可点击查看详情', '');
  }, 1500);
}

function addAPIDesign() {
  var path = document.getElementById('na-path').value;
  if (!path) { notify('请输入接口路径', 'err'); return; }
  state.apiDesigns.push({
    id: 'API-00' + (state.apiDesigns.length+1),
    method: document.getElementById('na-method').value,
    path: path,
    desc: document.getElementById('na-desc').value || '待补充',
    status: '设计中'
  });
  closeModal('modal-newapi');
  renderAPIDesign();
  notify('接口已添加', '');
}

// ===== API DOC =====
function renderAPIDoc() {
  var groups = [
    {name:'灌溉管理', apis:[{m:'GET',p:'/api/v1/irrigation/recommend'},{m:'POST',p:'/api/v1/irrigation/execute'},{m:'GET',p:'/api/v1/irrigation/history'}]},
    {name:'传感器数据', apis:[{m:'POST',p:'/api/v1/sensor/upload'},{m:'GET',p:'/api/v1/sensor/list'},{m:'DELETE',p:'/api/v1/sensor/{id}'}]},
    {name:'气象服务', apis:[{m:'GET',p:'/api/v1/weather/forecast'},{m:'GET',p:'/api/v1/weather/history'}]},
    {name:'用户管理', apis:[{m:'POST',p:'/api/v1/auth/login'},{m:'GET',p:'/api/v1/user/profile'}]}
  ];
  var el = document.getElementById('apiDocList');
  if (!el) return;
  var h = '';
  groups.forEach(function(g) {
    h += '<div style="margin-bottom:14px"><div style="font-weight:700;font-size:12.5px;color:var(--g700);margin-bottom:6px;padding:4px 0;border-bottom:1px solid var(--g200)">' + g.name + '</div>';
    g.apis.forEach(function(a) {
      h += '<div style="display:flex;align-items:center;gap:8px;padding:6px 8px;border-radius:6px;cursor:pointer;transition:all .15s;margin-bottom:3px" onmouseover="this.style.background=\'var(--g50)\'" onmouseout="this.style.background=\'\'" onclick="showAPIDebug(\'' + a.m + '\',\'' + a.p + '\')">';
      h += methodBadge(a.m) + ' <code style="font-size:11.5px">' + a.p + '</code></div>';
    });
    h += '</div>';
  });
  el.innerHTML = h;
}

function showAPIDebug(method, path) {
  var el = document.getElementById('apiDebugPanel');
  if (!el) return;
  var defaultBody = method === 'GET' ? '{\n  "field_code": "350200-FC-0342"\n}' : '{\n  "sensor_id": "SEN-001",\n  "moisture_pct": 23.4\n}';
  el.innerHTML = '<div style="margin-bottom:12px">' + methodBadge(method) + ' <code style="font-size:12px;background:var(--g100);padding:3px 8px;border-radius:5px">' + path + '</code></div>' +
    '<div class="fg"><label class="fl">基础URL</label><input class="fi" value="http://api.agri.local" style="font-family:monospace;font-size:12px"/></div>' +
    '<div class="fg"><label class="fl">请求参数 (JSON)</label><textarea class="fta" id="api-debug-body" style="font-family:monospace;font-size:11.5px;min-height:60px">' + defaultBody + '</textarea></div>' +
    '<button class="btn btn-p btn-sm w100" style="justify-content:center;margin-bottom:10px" onclick="runAPIDebug()">▶ 发送请求</button>' +
    '<div id="apiDebugResult"></div>';
}

function runAPIDebug() {
  var latency = 80 + Math.floor(Math.random() * 120);
  setTimeout(function() {
    var el = document.getElementById('apiDebugResult');
    if (el) el.innerHTML = '<div style="font-size:12px;font-weight:600;margin-bottom:5px;color:#10b981">✅ 200 OK · ' + latency + 'ms</div>' +
      '<div class="code" style="font-size:11px">{\n  "code": 200,\n  "msg": "success",\n  "data": {\n    "recommend_time": "06:30",\n    "water_amount": 12.5,\n    "confidence": 0.92\n  }\n}</div>';
    notify('接口调用成功 200 OK · ' + latency + 'ms', '');
  }, latency);
}

function syncAPIDoc() {
  notify('AI正在从GitLab代码注释同步API文档…', 'info');
  setTimeout(function() { notify('API文档已同步，新增3个接口，更新2个接口参数', ''); }, 2000);
}

// ===== TEST PLAN =====
function genTestPlan() {
  notify('AI正在生成测试方案…', 'info');
  setTimeout(function() {
    var el = document.getElementById('testPlanContent');
    if (!el) return;
    el.innerHTML = '<div class="prd">' +
      '<h3>📐 测试方案 · 智慧灌溉v2.1 Sprint3</h3>' +
      '<h4>一、测试范围</h4><ul><li>灌溉推荐AI接口（核心）</li><li>离线数据同步机制（高风险）</li><li>移动端灌溉控制页面</li></ul>' +
      '<h4>二、测试策略</h4><ul><li><strong>功能测试</strong>：基于需求用例，覆盖正常流/异常流/边界值</li><li><strong>接口测试</strong>：Postman自动化，覆盖全部API接口</li><li><strong>农业专项</strong>：弱网/断网/数据冲突场景专项测试</li></ul>' +
      '<h4>三、资源分配</h4><ul><li>测试周期：10天（2026-04-16 ~ 2026-04-25）</li><li>测试人员：2人</li></ul>' +
      '<h4>四、质量目标</h4><ul><li>P0缺陷上线前清零，P1缺陷修复率≥80%</li><li>自动化覆盖率≥60%</li></ul>' +
      '</div>';
    notify('测试方案已生成，测试周期10天', '');
  }, 2000);
}

// ===== TEST CASES =====
function renderTestCases(filter) {
  var rows = filter ? state.testcases.filter(function(t) { return t.status === filter; }) : state.testcases;
  var t = document.getElementById('tcTable');
  if (!t) return;
  var h = '<thead><tr><th style="width:32px"><input type="checkbox" id="tc-check-all" onchange="tcSelectAll(this.checked)"/></th><th>用例ID</th><th>用例标题</th><th>类型</th><th>优先级</th><th>状态</th><th>操作</th></tr></thead><tbody>';
  rows.forEach(function(tc) {
    var checked = state.selectedTCs[tc.id] ? 'checked' : '';
    h += '<tr><td><input type="checkbox" ' + checked + ' onchange="toggleTCSelect(\'' + tc.id + '\',this.checked)"/></td>';
    h += '<td style="font-size:11.5px;color:var(--g400)">' + tc.id + '</td>';
    h += '<td>' + tc.title + (tc.ai ? badge('bai','AI') : '') + '</td>';
    h += '<td>' + badge('bgr', tc.type) + '</td>';
    h += '<td>' + priLabel(tc.pri) + '</td>';
    h += '<td>' + badge(tc.status==='通过'?'bg':tc.status==='失败'?'brd':'bam', tc.status) + '</td>';
    h += '<td><button class="btn btn-s btn-sm" onclick="runSingleTC(\'' + tc.id + '\')">▶ 执行</button></td>';
    h += '</tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
  updateTCStats();
  updateBatchBar();
}

function updateTCStats() {
  var all = state.testcases;
  var passed = all.filter(function(t) { return t.status === '通过'; }).length;
  var failed = all.filter(function(t) { return t.status === '失败'; }).length;
  var pending = all.filter(function(t) { return t.status === '待执行'; }).length;
  var ai = all.filter(function(t) { return t.ai; }).length;
  setHTML('tcTotal', all.length);
  setHTML('tcAI', 'AI生成 ' + ai + ' 条');
  setHTML('tcPassed', passed);
  setHTML('tcPassRate', '通过率 ' + (all.length ? Math.round(passed/all.length*100) : '—') + '%');
  setHTML('tcPending', pending);
  setHTML('tcFailed', failed);
}

function filterTC(val) { renderTestCases(val); }

function tcSelectAll(checked) {
  state.testcases.forEach(function(t) { state.selectedTCs[t.id] = checked; });
  renderTestCases();
}

function toggleTCSelect(id, checked) {
  state.selectedTCs[id] = checked;
  updateBatchBar();
  var allCheck = document.getElementById('tc-check-all');
  if (allCheck) allCheck.checked = state.testcases.every(function(t) { return state.selectedTCs[t.id]; });
}

function updateBatchBar() {
  var cnt = Object.values(state.selectedTCs).filter(Boolean).length;
  var bar = document.getElementById('tc-batch-bar');
  var label = document.getElementById('tc-batch-count');
  if (bar) bar.classList.toggle('active', cnt > 0);
  if (label) label.textContent = '已选 ' + cnt + ' 条';
}

function batchRunTC() {
  var ids = Object.keys(state.selectedTCs).filter(function(id) { return state.selectedTCs[id]; });
  if (!ids.length) {
    ids = state.testcases.filter(function(t) { return t.status === '待执行'; }).map(function(t) { return t.id; });
  }
  if (!ids.length) { notify('请先选择要执行的用例', 'warn'); return; }
  notify('开始批量执行 ' + ids.length + ' 条用例…', 'info');
  var i = 0;
  var interval = setInterval(function() {
    if (i >= ids.length) {
      clearInterval(interval);
      state.selectedTCs = {};
      renderTestCases();
      notify('批量执行完成：' + ids.length + '条用例', '');
      return;
    }
    var tc = state.testcases.find(function(t) { return t.id === ids[i]; });
    if (tc) tc.status = Math.random() > 0.2 ? '通过' : '失败';
    i++;
  }, 300);
}

function batchDeleteTC() {
  var ids = Object.keys(state.selectedTCs).filter(function(id) { return state.selectedTCs[id]; });
  if (!ids.length) { notify('请先选择要删除的用例', 'warn'); return; }
  state.testcases = state.testcases.filter(function(t) { return !state.selectedTCs[t.id]; });
  state.selectedTCs = {};
  renderTestCases();
  notify('已删除 ' + ids.length + ' 条测试用例', 'warn');
}

function clearTCSelect() {
  state.selectedTCs = {};
  renderTestCases();
}

function genTestCases() {
  var panel = document.getElementById('tcGenPanel');
  if (panel) { panel.style.display = 'block'; panel.scrollIntoView({behavior:'smooth'}); }
}

function doGenTestCases() {
  var btn = document.getElementById('tcGenBtn');
  var prog = document.getElementById('tcGenProgress');
  if (btn) { btn.disabled = true; btn.textContent = '生成中…'; }
  if (prog) prog.style.display = 'block';
  var steps = ['分析需求文档REQ-089','生成正常流程用例（12条）','生成异常场景用例（18条）','生成农业专项用例（5条）'];
  var done = 0;
  var stepsEl = prog;
  if (stepsEl) stepsEl.innerHTML = '';
  function addStep() {
    if (stepsEl) {
      stepsEl.innerHTML += '<div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">✅ ' + steps[done] + '</div></div></div>';
    }
    done++;
    if (done < steps.length) { setTimeout(addStep, 800); }
    else {
      var newCases = [
        {id:'TC-0426',title:'土壤含水率0%边界情况下的推荐策略',type:'边界测试',pri:'P0',status:'待执行',ai:true},
        {id:'TC-0427',title:'同时多个地块请求推荐时的并发处理',type:'性能测试',pri:'P1',status:'待执行',ai:true},
        {id:'TC-0428',title:'传感器数据超出正常范围时的告警机制',type:'异常场景',pri:'P1',status:'待执行',ai:true},
        {id:'TC-0429',title:'弱网2G环境下灌溉推荐延迟测试',type:'农业专项',pri:'P0',status:'待执行',ai:true},
        {id:'TC-0430',title:'作物收割季节灌溉推荐自动停止逻辑',type:'农业专项',pri:'P1',status:'待执行',ai:true}
      ];
      state.testcases.push.apply(state.testcases, newCases);
      renderTestCases();
      if (btn) { btn.disabled = false; btn.textContent = '✨ 重新生成'; }
      notify('AI已生成 ' + newCases.length + ' 条新测试用例，包含农业专项场景', '');
    }
  }
  addStep();
}

function runSingleTC(id) {
  var tc = state.testcases.find(function(t) { return t.id === id; });
  if (!tc) return;
  tc.status = '待执行';
  renderTestCases();
  setTimeout(function() {
    tc.status = Math.random() > 0.25 ? '通过' : '失败';
    renderTestCases();
    notify('用例 ' + id + ' 执行完成：' + tc.status, '');
  }, 800);
}

function addManualTC() {
  var title = document.getElementById('nca-title').value;
  if (!title) { notify('请输入用例标题', 'err'); return; }
  state.testcases.unshift({
    id: 'TC-04' + (31 + state.testcases.length),
    title: title,
    type: document.getElementById('nca-type').value,
    pri: document.getElementById('nca-pri').value,
    status: '待执行',
    ai: false
  });
  closeModal('modal-testcase-add');
  renderTestCases();
  notify('测试用例已添加', '');
}

// ===== TEST DATA =====
function updateFieldSemantics() {
  var tableEl = document.getElementById('td-table');
  if (!tableEl) return;
  var table = tableEl.value;
  var semanticsMap = {
    't_soil_sensor_data（土壤传感器数据）': [{f:'field_code',s:'地块编号（农田行政区划格式）'},{f:'moisture_pct',s:'土壤含水率（10%-45%正常范围）'},{f:'crop_name',s:'作物品种（引用AgriKB品种库）'}],
    't_weather_record（气象记录）': [{f:'temperature',s:'气温（-10℃ ~ 45℃农业范围）'},{f:'et0',s:'蒸散量（0-15mm/day）'},{f:'rainfall',s:'降雨量（0-300mm/day）'}],
    't_crop_info（作物信息）': [{f:'crop_name',s:'作物名称（引用AgriKB 300+品种）'},{f:'growth_stage',s:'生长阶段（播种/幼苗/分蘖/成熟）'}],
    't_pest_record（病虫害记录）': [{f:'pest_name',s:'病虫害名称（引用AgriKB病虫害库）'},{f:'severity',s:'危害程度（轻/中/重）'}],
    't_irrigation_plan（灌溉计划）': [{f:'recommend_time',s:'推荐灌溉时间（农业最优时段）'},{f:'water_amount',s:'灌溉量（mm，基于ET₀计算）'}]
  };
  var list = semanticsMap[table] || [];
  var el = document.getElementById('fieldSemantics');
  if (!el) return;
  var h = '';
  list.forEach(function(s) {
    h += '<div style="display:flex;align-items:center;gap:7px;margin-bottom:5px">';
    h += badge('bbl', s.f) + ' <span style="color:var(--g400)">→</span> ' + badge('bai', '🤖 ' + s.s);
    h += '</div>';
  });
  el.innerHTML = h;
}

function generateTestData() {
  var countEl = document.getElementById('td-count');
  var formatEl = document.getElementById('td-format');
  var tableEl = document.getElementById('td-table');
  var count = countEl ? parseInt(countEl.value) || 1000 : 1000;
  var format = formatEl ? formatEl.value : 'JSON';
  var table = tableEl ? tableEl.value.split('（')[0] : 't_data';
  var badge_el = document.getElementById('tdBadge');
  var preview = document.getElementById('tdPreview');
  var actions = document.getElementById('tdActions');
  if (badge_el) { badge_el.className = 'b bam'; badge_el.textContent = '生成中…'; }
  if (preview) preview.innerHTML = '<div class="ai-gen" style="justify-content:center;padding:20px">🤖 AI生成中 <span class="dots"><span></span><span></span><span></span></span></div>';
  notify('正在生成 ' + count + ' 条 ' + table + ' 数据…', 'info');
  setTimeout(function() {
    if (badge_el) { badge_el.className = 'b bg'; badge_el.textContent = '已生成 ' + count + ' 条'; }
    var content = '';
    if (format === 'CSV') {
      content = 'field_code,moisture_pct,temperature,record_time\n350200-FC-0342,23.4,22.1,2026-04-15 08:23\n410500-FC-1208,18.9,19.8,2026-04-15 08:24\n// ... 共' + count + '条';
    } else if (format === 'SQL INSERT') {
      content = 'INSERT INTO ' + table + '(field_code,moisture_pct,...) VALUES\n(\'350200-FC-0342\',23.4,...),\n(\'410500-FC-1208\',18.9,...),\n-- ... 共' + count + '条';
    } else {
      content = '[\n  {"field_code":"350200-FC-0342","moisture_pct":23.4,"temperature":22.1},\n  {"field_code":"410500-FC-1208","moisture_pct":18.9,"temperature":19.8},\n  // ... 共' + count + '条，符合中国农田地理范围\n]';
    }
    if (preview) preview.innerHTML = '<div class="code" style="font-size:11px;max-height:200px;overflow-y:auto">' + content.replace(/</g,'&lt;') + '</div>';
    if (actions) actions.style.display = 'block';
    notify(count + '条测试数据已生成，格式：' + format, '');
  }, 1800);
}

function writeDataToDB() { openModal('modal-db-write'); }

function startDBWrite() {
  var btn = document.getElementById('dbw-start-btn');
  var progEl = document.getElementById('dbw-progress');
  var resultEl = document.getElementById('dbw-result');
  var bar = document.getElementById('dbw-bar');
  var stage = document.getElementById('dbw-stage');
  var log = document.getElementById('dbw-log');
  var count = document.getElementById('td-count') ? (document.getElementById('td-count').value || '1000') : '1000';
  var table = document.getElementById('td-table') ? document.getElementById('td-table').value.split('（')[0] : 't_data';
  var target = document.getElementById('dbw-target') ? document.getElementById('dbw-target').value : 'TEST';
  if (btn) btn.disabled = true;
  if (progEl) progEl.style.display = 'block';
  if (resultEl) resultEl.style.display = 'none';
  var stages = [
    {pct:15, text:'连接数据库中…', log:nowStr() + ' CONNECT OK'},
    {pct:35, text:'校验表结构…', log:nowStr() + ' DESCRIBE ' + table + ' → OK'},
    {pct:65, text:'批量写入数据…', log:nowStr() + ' INSERT batch 1/3 → ' + Math.floor(parseInt(count)/3) + ' rows'},
    {pct:85, text:'提交事务…', log:nowStr() + ' COMMIT → ' + count + ' rows'},
    {pct:100, text:'写入完成 ✅', log:nowStr() + ' DONE in 0.83s'}
  ];
  var i = 0;
  var interval = setInterval(function() {
    if (i >= stages.length) {
      clearInterval(interval);
      if (resultEl) {
        resultEl.style.display = 'block';
        resultEl.innerHTML = '<div style="background:var(--gpale);border-radius:8px;padding:12px;border-left:3px solid var(--gp)"><div style="font-weight:700;font-size:13px;color:var(--gp);margin-bottom:5px">✅ 数据写入成功</div><div style="font-size:12px;color:var(--g600)">共写入 <strong>' + count + '</strong> 条记录到 <strong>' + table + '</strong><br>目标：' + target.split('(')[0].trim() + ' · 耗时 0.83秒</div></div>';
      }
      if (btn) { btn.disabled = false; btn.textContent = '✅ 已完成'; }
      notify(count + '条数据已写入数据库', '');
      return;
    }
    var s = stages[i];
    if (bar) bar.style.width = s.pct + '%';
    if (stage) stage.textContent = s.text;
    if (log) log.innerHTML = (log.innerHTML ? log.innerHTML + '<br>' : '') + '<span style="color:var(--g400)">' + s.log + '</span>';
    i++;
  }, 600);
}

// ===== SUBMIT =====
function renderSubmit() {
  var t = document.getElementById('submitTable');
  if (!t) return;
  var h = '<thead><tr><th>提测单</th><th>标题</th><th>提交人</th><th>日期</th><th>门禁</th><th>状态</th><th>操作</th></tr></thead><tbody>';
  state.submitList.forEach(function(s) {
    h += '<tr>';
    h += '<td style="font-size:11.5px;color:var(--g400)">' + s.id + '</td>';
    h += '<td>' + s.title + '</td>';
    h += '<td>' + s.submitter + '</td>';
    h += '<td>' + s.date + '</td>';
    h += '<td>' + badge(s.gate==='通过'?'bg':'brd', s.gate==='通过'?'✅ 通过':'❌ 未通过') + '</td>';
    h += '<td>' + badge(s.status==='测试中'?'bbl':s.status==='待审批'?'bam':s.status==='已驳回'?'brd':'bg', s.status) + '</td>';
    h += '<td style="white-space:nowrap"><button class="btn btn-s btn-sm" onclick="openSubmitDetail(\'' + s.id + '\')">查看</button>';
    if (s.status === '待审批') h += ' <button class="btn btn-p btn-sm" onclick="approveSubmit(\'' + s.id + '\')">✅ 审批</button>';
    h += '</td></tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
}

function openSubmitDetail(id) {
  var s = state.submitList.find(function(x) { return x.id === id; });
  if (!s) return;
  state.currentSubmitId = id;
  document.getElementById('sdm-title').textContent = s.title;
  document.getElementById('sdm-meta').textContent = s.id + ' · 提交人：' + s.submitter + ' · ' + s.date;
  document.getElementById('sdm-scope').innerHTML = '<div style="line-height:1.7">✅ 灌溉推荐AI接口（核心功能）<br>✅ 离线数据同步机制<br>✅ 移动端灌溉控制页面<br>⚠️ 传感器数据接入（部分）</div>';
  document.getElementById('sdm-env').innerHTML = '<div style="font-weight:600;margin-bottom:4px">测试环境 TEST</div><div style="color:var(--g500)">test.agri.local:8080</div><div style="color:var(--g500)">期望周期：3天</div>';
  document.getElementById('sdm-defect-summary').innerHTML = '<div style="font-size:12.5px;font-weight:700;margin-bottom:8px">📋 关联缺陷</div><div class="g4"><div class="sc"><div class="sl">P0 致命</div><div class="sv" style="color:var(--rd)">0</div></div><div class="sc"><div class="sl">P1 严重</div><div class="sv" style="color:var(--am)">2</div></div><div class="sc"><div class="sl">P2 一般</div><div class="sv">5</div></div><div class="sc"><div class="sl">已修复</div><div class="sv" style="color:#10b981">6</div></div></div>';
  var gateItems = [
    {ok:true, text:'单测覆盖率 67%（要求≥60%）'},
    {ok:true, text:'SonarQube：0高危漏洞'},
    {ok:s.gate==='通过', text:'PRD文档完整性 ' + (s.gate==='通过'?'96%（通过）':'52%（未通过）')},
    {ok:true, text:'接口文档已更新'}
  ];
  var gateH = '<div class="tl">';
  gateItems.forEach(function(g) {
    gateH += '<div class="tli"><div class="tld ' + (g.ok?'done':'active') + '"></div><div class="tlc"><div class="tlt">' + (g.ok?'✅':'❌') + ' ' + g.text + '</div></div></div>';
  });
  gateH += '</div>';
  document.getElementById('sdm-gate-detail').innerHTML = gateH;
  var approveH = '<div class="tl"><div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">提测申请已提交</div><div class="tls">' + s.submitter + ' · ' + s.date + '</div></div></div>';
  if (s.status !== '待审批') approveH += '<div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">已审批</div><div class="tls">张总 · ' + s.date + '</div></div></div>';
  approveH += '</div>';
  document.getElementById('sdm-approval-log').innerHTML = approveH;
  var approveBtn = document.getElementById('sdm-approve-btn');
  var rejectBtn = document.getElementById('sdm-reject-btn');
  if (approveBtn) { approveBtn.disabled = s.status !== '待审批'; approveBtn.textContent = s.status !== '待审批' ? '已审批' : '✅ 审批通过'; }
  if (rejectBtn) rejectBtn.disabled = s.status !== '待审批';
  // Reset to tab 0
  var modal = document.getElementById('modal-submitdetail');
  if (modal) {
    modal.querySelectorAll('.tab').forEach(function(t,i) { t.classList.toggle('active', i===0); });
    modal.querySelectorAll('.tp').forEach(function(p,i) { p.classList.toggle('active', i===0); });
  }
  openModal('modal-submitdetail');
}

function approveFromDetail() {
  var s = state.submitList.find(function(x) { return x.id === state.currentSubmitId; });
  if (s) s.status = '测试中';
  closeModal('modal-submitdetail');
  renderSubmit();
  notify('提测单 ' + state.currentSubmitId + ' 已审批通过', '');
}
function rejectSubmit() {
  var s = state.submitList.find(function(x) { return x.id === state.currentSubmitId; });
  if (s) { s.status = '已驳回'; s.gate = '未通过'; }
  closeModal('modal-submitdetail');
  renderSubmit();
  notify('提测单已驳回，请修复后重新提测', 'err');
}

function runSubmitGate() {
  var res = document.getElementById('submitGateResult');
  if (res) res.style.display = 'block';
  if (res) res.innerHTML = '<div class="ai-gen" style="justify-content:center;padding:10px">🔒 AI门禁检查中 <span class="dots"><span></span><span></span><span></span></span></div>';
  notify('AI正在执行质量门禁检查…', 'info');
  setTimeout(function() {
    var passed = Math.random() > 0.4;
    var items = [
      {ok:true, text:'单测覆盖率：67%（≥60% ✓）'},
      {ok:true, text:'SonarQube扫描：0高危漏洞 ✓'},
      {ok:passed, text:'PRD文档完整性：' + (passed?'96%（通过）':'52%（未通过）')},
      {ok:true, text:'接口文档已更新 ✓'}
    ];
    var h = '<div style="background:' + (passed?'var(--gpale)':'var(--rdl)') + ';border-radius:8px;padding:12px;border-left:3px solid ' + (passed?'var(--gp)':'var(--rd)') + '">';
    h += '<div style="font-weight:700;font-size:13px;margin-bottom:8px">' + (passed?'✅ 门禁检查通过':'❌ 门禁检查未通过') + '</div>';
    h += '<div style="display:flex;flex-direction:column;gap:5px;font-size:12px">';
    items.forEach(function(item) { h += '<div>' + (item.ok?'✅':'❌') + ' ' + item.text + '</div>'; });
    h += '</div></div>';
    if (res) res.innerHTML = h;
    var confirmBtn = document.getElementById('submitConfirmBtn');
    if (passed) {
      if (confirmBtn) confirmBtn.style.display = 'inline-flex';
      notify('质量门禁全部通过，可以提测', '');
    } else {
      notify('质量门禁未通过，请修复后重试', 'err');
    }
  }, 2000);
}

function confirmSubmit() {
  var title = document.getElementById('ns-title') ? document.getElementById('ns-title').value : '新提测单';
  state.submitList.unshift({id:'ST-0' + (13 + state.submitList.length), title:title || '新提测单', status:'待审批', submitter:'张总', date:dateStr(), gate:'通过'});
  closeModal('modal-newsubmit');
  renderSubmit();
  notify('提测单已提交，测试经理将收到飞书审批通知', '');
}

function approveSubmit(id) {
  var s = state.submitList.find(function(x) { return x.id === id; });
  if (s) { s.status = '测试中'; renderSubmit(); notify('提测单 ' + id + ' 已审批通过', ''); }
}

// ===== AUTO TEST =====
function renderAutoTest() {
  var t = document.getElementById('autotestTable');
  if (!t) return;
  var h = '<thead><tr><th>套件名称</th><th>类型</th><th>用例数</th><th>上次执行</th><th>通过率</th><th>操作</th></tr></thead><tbody>';
  state.autoTests.forEach(function(a) {
    h += '<tr><td>' + a.name + (a.status==='失败'?' '+badge('brd','失败'):'') + '</td>';
    h += '<td>' + badge('bgr', a.type) + '</td>';
    h += '<td>' + a.cases + '</td>';
    h += '<td style="font-size:11.5px">' + a.lastRun + '</td>';
    h += '<td><div style="display:flex;align-items:center;gap:6px"><div class="pb" style="width:55px"><div class="pf ' + (a.passRate>=90?'pfg':a.passRate>=70?'pfam':'pfrd') + '" style="width:' + a.passRate + '%"></div></div><span style="font-size:12px;font-weight:600">' + a.passRate + '%</span></div></td>';
    h += '<td><button class="btn btn-p btn-sm" onclick="runSingleSuite(\'' + a.id + '\')">▶</button></td></tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
  var total = state.autoTests.reduce(function(s, a) { return s+a.cases; }, 0);
  var passAvg = Math.round(state.autoTests.reduce(function(s,a) { return s+a.passRate; }, 0) / state.autoTests.length);
  var resEl = document.getElementById('autoTestResult');
  if (resEl) {
    var resH = '<div style="margin-bottom:12px"><div style="display:flex;justify-content:space-between;font-size:12.5px;margin-bottom:6px"><span>总执行 <strong>' + total + '</strong> 个用例</span><span>综合通过率 <strong style="color:' + (passAvg>=85?'var(--gp)':passAvg>=70?'var(--am)':'var(--rd)') + '">' + passAvg + '%</strong></span></div>';
    resH += '<div class="tr-bar"><div class="tr-pass" style="width:' + passAvg + '%"></div><div class="tr-fail" style="width:' + (100-passAvg) + '%"></div></div></div>';
    resH += '<div class="tl">';
    state.autoTests.forEach(function(a) {
      resH += '<div class="tli"><div class="tld ' + (a.passRate>=90?'done':a.passRate>=70?'active':'') + '"></div><div class="tlc"><div class="tlt">' + a.name + '</div><div class="tls">' + a.passRate + '% 通过 · ' + a.cases + ' 用例 · ' + a.lastRun + '</div></div></div>';
    });
    resH += '</div>';
    resEl.innerHTML = resH;
  }
}

function runAutoTests() {
  notify('开始执行全量自动化测试套件…', 'info');
  var i = 0;
  var interval = setInterval(function() {
    if (i >= state.autoTests.length) {
      clearInterval(interval);
      renderAutoTest();
      notify('全量自动化测试执行完成', '');
      return;
    }
    var a = state.autoTests[i];
    a.passRate = Math.min(100, Math.max(0, a.passRate + Math.floor(Math.random()*7-3)));
    a.lastRun = '2026-04-15 ' + new Date().toTimeString().slice(0,5);
    a.status = a.passRate >= 70 ? '就绪' : '失败';
    i++;
  }, 600);
}

function runSingleSuite(id) {
  var a = state.autoTests.find(function(x) { return x.id === id; });
  if (!a) return;
  notify('正在执行：' + a.name + '…', 'info');
  setTimeout(function() {
    a.passRate = Math.min(100, a.passRate + Math.floor(Math.random()*5-1));
    a.lastRun = '2026-04-15 ' + new Date().toTimeString().slice(0,5);
    a.status = a.passRate >= 70 ? '就绪' : '失败';
    renderAutoTest();
    notify(a.name + ' 执行完成，通过率 ' + a.passRate + '%', '');
  }, 1500);
}

function genAutoScript() {
  notify('AI正在分析测试用例，生成Playwright自动化脚本…', 'info');
  setTimeout(function() { notify('已生成12个自动化测试脚本，新增灌溉API测试套件', ''); }, 2000);
}

// ===== DEFECTS =====
function renderDefects(sFilter, lFilter) {
  var rows = state.defects;
  if (sFilter) rows = rows.filter(function(d) { return d.status === sFilter; });
  if (lFilter) rows = rows.filter(function(d) { return d.level === lFilter; });
  var t = document.getElementById('defectTable');
  if (!t) return;
  var levCls = {P0:'sev-p0', P1:'sev-p1', P2:'sev-p2', P3:'sev-p3'};
  var h = '<thead><tr><th>缺陷ID</th><th>缺陷标题</th><th>级别</th><th>模块</th><th>状态</th><th>指派</th><th>操作</th></tr></thead><tbody>';
  rows.forEach(function(d) {
    h += '<tr><td style="font-size:11.5px;color:var(--g400)">' + d.id + '</td>';
    h += '<td>' + d.title + '</td>';
    h += '<td><span class="b ' + (levCls[d.level]||'') + '">' + d.level + '</span></td>';
    h += '<td>' + badge('bgr', d.module) + '</td>';
    h += '<td>' + statusBadge(d.status) + '</td>';
    h += '<td>' + d.owner + '</td>';
    h += '<td style="white-space:nowrap"><button class="btn btn-s btn-sm" onclick="openDefectEdit(\'' + d.id + '\')">✏️</button>';
    if (d.status !== '已关闭') h += ' <button class="btn btn-p btn-sm" onclick="closeDefect(\'' + d.id + '\')">✅</button>';
    h += '</td></tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
  setHTML('defTotal', state.defects.length);
  setHTML('defCritical', state.defects.filter(function(d) { return d.level==='P0'||d.level==='P1'; }).length);
  setHTML('defFix', state.defects.filter(function(d) { return d.status==='修复中'; }).length);
  setHTML('defClosed', state.defects.filter(function(d) { return d.status==='已关闭'; }).length);
  renderDefectTrend();
}

function renderDefectTrend() {
  var el = document.getElementById('defect-trend-chart');
  if (!el) return;
  var days = ['4/9','4/10','4/11','4/12','4/13','4/14','4/15'];
  var newCounts = [5,3,7,4,2,6,3];
  var resolvedCounts = [3,5,4,6,4,3,5];
  var max = 10;
  var h = '<div style="font-size:11px;color:var(--g500);margin-bottom:5px;display:flex;gap:12px">';
  h += '<span>■ <span style="color:var(--rd)">新增</span></span><span>■ <span style="color:#10b981">关闭</span></span></div>';
  h += '<div style="display:flex;gap:6px;align-items:flex-end;height:70px">';
  days.forEach(function(d, i) {
    h += '<div style="flex:1;display:flex;flex-direction:column;align-items:center;gap:2px">';
    h += '<div style="width:100%;display:flex;gap:2px;align-items:flex-end;height:55px">';
    h += '<div style="flex:1;background:var(--rd);border-radius:2px 2px 0 0;height:' + (newCounts[i]/max*55) + 'px" title="' + d + ' 新增' + newCounts[i] + '"></div>';
    h += '<div style="flex:1;background:#10b981;border-radius:2px 2px 0 0;height:' + (resolvedCounts[i]/max*55) + 'px" title="' + d + ' 关闭' + resolvedCounts[i] + '"></div>';
    h += '</div><div style="font-size:10px;color:var(--g400)">' + d + '</div></div>';
  });
  h += '</div>';
  el.innerHTML = h;

  var distEl = document.getElementById('defect-dist-chart');
  if (!distEl) return;
  var dist = [{l:'P0致命',cnt:2,color:'#be123c'},{l:'P1严重',cnt:6,color:'var(--rd)'},{l:'P2一般',cnt:7,color:'var(--am)'},{l:'P3轻微',cnt:3,color:'#10b981'}];
  var tot = dist.reduce(function(s,d) { return s+d.cnt; }, 0);
  var dh = '';
  dist.forEach(function(d) {
    dh += '<div style="display:flex;align-items:center;gap:8px;margin-bottom:8px">';
    dh += '<span style="width:50px;font-size:11.5px;font-weight:600;color:' + d.color + '">' + d.l + '</span>';
    dh += '<div class="pb" style="flex:1"><div class="pf" style="width:' + (d.cnt/tot*100) + '%;background:' + d.color + '"></div></div>';
    dh += '<span style="font-size:12px;width:20px;font-weight:700;color:' + d.color + '">' + d.cnt + '</span></div>';
  });
  distEl.innerHTML = dh;
}

function filterDefects(val, type) {
  var sFilter = type === 'status' ? val : '';
  var lFilter = type === 'level' ? val : '';
  renderDefects(sFilter, lFilter);
}

function closeDefect(id) {
  var d = state.defects.find(function(x) { return x.id === id; });
  if (d) { d.status = '已关闭'; renderDefects(); notify('缺陷 ' + id + ' 已关闭', ''); }
}

function openDefectEdit(id) {
  var d = state.defects.find(function(x) { return x.id === id; });
  if (!d) return;
  document.getElementById('dem-title').textContent = '编辑缺陷 · ' + id;
  document.getElementById('dem-title-input').value = d.title;
  document.getElementById('dem-module').value = d.module;
  document.getElementById('dem-note').value = d.note || '';
  document.getElementById('dem-editing-id').value = id;
  setSelectValue('dem-level', d.level);
  setSelectValue('dem-status', d.status);
  setSelectValue('dem-owner', d.owner);
  openModal('modal-defectedit');
}

function saveDefectEdit() {
  var id = document.getElementById('dem-editing-id').value;
  var d = state.defects.find(function(x) { return x.id === id; });
  if (!d) return;
  d.title = document.getElementById('dem-title-input').value || d.title;
  d.level = document.getElementById('dem-level').value.split(' - ')[0];
  d.status = document.getElementById('dem-status').value;
  d.module = document.getElementById('dem-module').value;
  d.owner = document.getElementById('dem-owner').value;
  d.note = document.getElementById('dem-note').value;
  closeModal('modal-defectedit');
  renderDefects();
  notify('缺陷 ' + id + ' 已更新', '');
}

function confirmDeleteDefect() {
  var id = document.getElementById('dem-editing-id').value;
  var idx = state.defects.findIndex(function(x) { return x.id === id; });
  if (idx > -1) state.defects.splice(idx, 1);
  closeModal('modal-defectedit');
  renderDefects();
  notify('缺陷 ' + id + ' 已删除', 'warn');
}

function addDefect() {
  var title = document.getElementById('nd-title').value;
  if (!title) { notify('请输入缺陷标题', 'err'); return; }
  var levMap = {'P0 - 致命':'P0','P1 - 严重':'P1','P2 - 一般':'P2','P3 - 轻微':'P3'};
  state.defects.unshift({
    id: 'BUG-' + (157 + state.defects.length),
    title: title,
    level: levMap[document.getElementById('nd-level').value] || 'P2',
    status: '待确认',
    owner: document.getElementById('nd-owner').value,
    module: '待分类',
    note: ''
  });
  closeModal('modal-newdefect');
  renderDefects();
  notify('缺陷"' + title + '"已提交', '');
}

function aiDefectMatch() {
  var el = document.getElementById('defectSimMatch');
  if (el) { el.style.display = 'block'; el.innerHTML = '⚠️ AI检测到相似缺陷：<strong>BUG-156（断网同步后数据重复）</strong>可能相关，建议先查看是否为同一问题。'; }
}

// ===== TEST REPORT =====
function genTestReport() {
  notify('AI正在汇总测试数据，生成测试报告…', 'info');
  var total = state.testcases.length;
  var passed = state.testcases.filter(function(t) { return t.status === '通过'; }).length;
  var failed = state.testcases.filter(function(t) { return t.status === '失败'; }).length;
  var passRate = total ? Math.round(passed/total*100) : 0;
  setTimeout(function() {
    var el = document.getElementById('testReportContent');
    if (!el) return;
    var h = '<div class="card"><div class="ct">📊 测试报告 · 智慧灌溉v2.1 Sprint3 · AI生成</div>';
    h += '<div style="display:flex;gap:6px;margin-bottom:10px">' + badge('bai','🤖 AI生成') + badge(passRate>=80?'bg':'bam',passRate>=80?'✅ 建议上线':'⚠️ 谨慎上线') + '</div>';
    h += '<div class="g4 mb3"><div class="sc"><div class="sl">执行用例</div><div class="sv" style="color:var(--gp)">' + total + '</div></div><div class="sc"><div class="sl">通过</div><div class="sv" style="color:#10b981">' + passed + '</div></div><div class="sc"><div class="sl">失败</div><div class="sv" style="color:var(--rd)">' + failed + '</div></div><div class="sc"><div class="sl">通过率</div><div class="sv" style="color:var(--bl)">' + passRate + '%</div></div></div>';
    h += '<div style="margin-bottom:14px"><div class="tr-bar" style="height:12px;border-radius:6px"><div class="tr-pass" style="width:' + passRate + '%"></div><div class="tr-fail" style="width:' + (100-passRate) + '%"></div></div></div>';
    h += '<div class="g2"><div>';
    h += '<div style="font-size:12.5px;font-weight:700;margin-bottom:8px">📋 缺陷分布</div>';
    var defDist = [{l:'P0 致命',cnt:2,cls:'pfrd'},{l:'P1 严重',cnt:6,cls:'pfrd'},{l:'P2 一般',cls:'pfam',cnt:7},{l:'P3 轻微',cnt:3,cls:'pfg'}];
    defDist.forEach(function(d) {
      h += '<div style="display:flex;align-items:center;gap:8px;margin-bottom:6px"><span style="width:60px;font-size:12px">' + d.l + '</span><div class="pb" style="flex:1"><div class="pf ' + d.cls + '" style="width:' + (d.cnt/18*100) + '%"></div></div><span style="font-size:12px;width:20px">' + d.cnt + '</span></div>';
    });
    h += '</div><div>';
    h += '<div style="font-size:12.5px;font-weight:700;margin-bottom:8px">🤖 AI质量评估</div>';
    h += '<div style="background:' + (passRate>=80?'var(--gpale)':'var(--aml)') + ';border-radius:8px;padding:12px;font-size:12.5px">';
    h += '<div style="font-weight:700;font-size:14px;margin-bottom:6px">' + (passRate>=80?'🟢 建议上线':'🟡 谨慎评估') + '</div>';
    h += '<div style="color:var(--g600)">P0缺陷已全部修复，P1遗留' + state.defects.filter(function(d) { return d.level==='P1'&&d.status!=='已关闭'; }).length + '个，建议重点关注离线同步模块稳定性。上线风险评级：<strong>' + (passRate>=80?'低':'中') + '</strong></div></div>';
    h += '</div></div>';
    h += '<div class="btn-row mt3"><button class="btn btn-p btn-sm" onclick="exportTestReportPDF()">📄 导出PDF</button><button class="btn btn-s btn-sm" onclick="shareTestReport()">🔗 分享链接</button></div></div>';
    el.innerHTML = h;
    notify('测试报告已生成，上线风险评级：' + (passRate>=80?'低':'中'), '');
  }, 2500);
}

// ===== UED =====
function renderUED() {
  var versions = state.uedVersions;
  var t = document.getElementById('uedVersions');
  if (!t) return;
  var h = '<thead><tr><th>版本</th><th>更新内容</th><th>提交人</th><th>日期</th><th>状态</th><th>操作</th></tr></thead><tbody>';
  versions.forEach(function(v) {
    h += '<tr><td><strong>' + v.v + '</strong></td><td>' + v.desc + '</td><td>' + v.author + '</td><td>' + v.date + '</td>';
    h += '<td>' + badge(v.status==='最新'?'bg':'bgr', v.status) + '</td>';
    h += '<td><button class="btn btn-s btn-sm" onclick="openDesignPreview(\'' + v.v + '\',\'' + v.desc + '\')">查看</button></td></tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
}

function runUEDCheck() {
  var review = document.getElementById('uedReview');
  if (!review) return;
  review.innerHTML = '<div class="ai-gen" style="justify-content:center;padding:20px">🤖 AI设计规范检查中 <span class="dots"><span></span><span></span><span></span></span></div>';
  notify('AI正在检查设计规范遵从度…', 'info');
  setTimeout(function() {
    review.innerHTML = '<div style="display:flex;gap:6px;margin-bottom:12px">' + badge('bg','✅ 规范评分：88分') + badge('bai','AI评审完成') + '</div>' +
      '<div class="tl">' +
      '<div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">✅ 颜色规范检查通过</div><div class="tls">主色调 #2d7a4f 在所有组件中一致使用</div></div></div>' +
      '<div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">✅ 字体规范检查通过</div><div class="tls">标题/正文/辅助文字层级清晰</div></div></div>' +
      '<div class="tli"><div class="tld active"></div><div class="tlc"><div class="tlt">⚠️ 间距不一致（第3屏）</div><div class="tls">灌溉控制按钮间距为12px，其他区域为16px，建议统一</div></div></div>' +
      '<div class="tli"><div class="tld active"></div><div class="tlc"><div class="tlt">⚠️ 无障碍：2个元素对比度不足</div><div class="tls">浅灰色文字对比度3.4:1，不达WCAG AA标准（4.5:1）</div></div></div>' +
      '</div>' +
      '<button class="btn btn-p btn-sm mt3" onclick="syncToFigma()">📤 同步至Figma</button>';
    notify('设计规范检查完成，发现2个待优化项', 'warn');
  }, 2000);
}

// ===== ANALYTICS =====
function renderAnalytics() {
  var stages = [{name:'需求/PRD',manual:8,ai:1.5},{name:'架构设计',manual:16,ai:3},{name:'测试用例',manual:20,ai:4},{name:'测试数据',manual:12,ai:0.5},{name:'文档生成',manual:24,ai:2}];
  var effEl = document.getElementById('aiEfficiencyChart');
  if (effEl) {
    var h = '<div style="font-size:12px;color:var(--g500);margin-bottom:10px">各阶段耗时对比（小时）：手工 vs AI辅助</div>';
    stages.forEach(function(s) {
      h += '<div style="margin-bottom:10px"><div style="font-size:12px;font-weight:600;margin-bottom:3px">' + s.name + '</div>';
      h += '<div style="display:flex;align-items:center;gap:6px;margin-bottom:2px"><span style="width:55px;font-size:11px;color:var(--g500)">手工</span><div class="pb" style="flex:1"><div class="pf" style="width:' + (s.manual/24*100) + '%;background:var(--g300)"></div></div><span style="font-size:11.5px;width:30px">' + s.manual + 'h</span></div>';
      h += '<div style="display:flex;align-items:center;gap:6px"><span style="width:55px;font-size:11px;color:var(--pu)">AI辅助</span><div class="pb" style="flex:1"><div class="pf pfpu" style="width:' + (s.ai/24*100) + '%;"></div></div><span style="font-size:11.5px;width:30px;color:var(--pu);font-weight:700">' + s.ai + 'h</span></div></div>';
    });
    h += '<div style="background:linear-gradient(135deg,#ede9fe,#dbeafe);border-radius:8px;padding:10px;font-size:12.5px;font-weight:600;color:var(--pu);text-align:center;margin-top:10px">🤖 AI平均提效 <strong>83%</strong>，本月节省 <strong>284小时</strong></div>';
    effEl.innerHTML = h;
  }
  var healthEl = document.getElementById('projectHealthChart');
  if (healthEl) {
    var hh = '';
    state.projects.forEach(function(p) {
      hh += '<div style="display:flex;align-items:center;gap:10px;padding:8px 0;border-bottom:1px solid var(--g100)">';
      hh += '<div style="width:8px;height:8px;border-radius:50%;background:' + healthColor(p.health) + ';flex-shrink:0"></div>';
      hh += '<div style="flex:1;font-size:12.5px">' + p.name + '</div>';
      hh += progressBar(p.progress, p.health==='green'?'pfg':p.health==='amber'?'pfam':'pfrd');
      hh += '<span style="font-size:12px;font-weight:700;width:36px">' + p.progress + '%</span></div>';
    });
    healthEl.innerHTML = hh;
  }
  var sugEl = document.getElementById('aiSuggestions');
  if (sugEl) {
    var sug = [
      {level:'red',emoji:'🔴',title:'病虫害AI模块存在严重进度风险',desc:'完成率22%，AI预测交付概率41%。建议：增加1名AI算法工程师，或拆分v1/v2分期交付。'},
      {level:'amber',emoji:'🟡',title:'测试用例覆盖率低于目标(76% < 90%)',desc:'建议立即启用AI批量生成测试用例，预计3天内可补充150条覆盖缺口模块。'},
      {level:'green',emoji:'🟢',title:'溯源平台即将完成，建议提前准备交付材料',desc:'完成率92%。AI建议：立即启动运维手册生成、实施人员培训材料准备。'}
    ];
    var sh = '';
    sug.forEach(function(s) {
      sh += '<div style="display:flex;gap:12px;padding:12px;background:' + (s.level==='red'?'var(--rdl)':s.level==='amber'?'var(--aml)':'var(--gpale)') + ';border-radius:8px;border-left:4px solid ' + (s.level==='red'?'var(--rd)':s.level==='amber'?'var(--am)':'var(--gp)') + ';margin-bottom:8px">';
      sh += '<span style="font-size:18px">' + s.emoji + '</span><div><div style="font-weight:700;font-size:12.5px;margin-bottom:4px">' + s.title + '</div><div style="font-size:12px;color:var(--g600)">' + s.desc + '</div></div></div>';
    });
    sugEl.innerHTML = sh;
  }
}

// ===== SETTINGS =====
function renderSettings() {
  var models = [
    {name:'DeepSeek-V3（主模型）',type:'私有化部署',use:'PRD生成/需求分析/文档生成',status:'✅ 已连接'},
    {name:'Claude Sonnet 4.6',type:'API调用',use:'复杂推理/架构设计/代码生成',status:'✅ 已连接'},
    {name:'DeepSeek-R1（推理）',type:'私有化部署',use:'技术方案评估/测试策略',status:'✅ 已连接'}
  ];
  var mt = document.getElementById('modelConfigTable');
  if (mt) {
    var h = '<thead><tr><th>模型名称</th><th>部署方式</th><th>用途</th><th>状态</th><th>操作</th></tr></thead><tbody>';
    models.forEach(function(m) {
      h += '<tr><td><strong>' + m.name + '</strong></td><td>' + badge('bgr',m.type) + '</td><td style="font-size:12px">' + m.use + '</td><td style="color:#10b981">' + m.status + '</td><td><button class="btn btn-s btn-sm" onclick="testModelConn(this)">测试连接</button></td></tr>';
    });
    h += '</tbody>';
    mt.innerHTML = h;
  }
  var flows = [
    {name:'prd-generation-flow',desc:'PRD自动生成',trigger:'手动/API',status:'运行中'},
    {name:'testcase-gen-flow',desc:'测试用例生成',trigger:'手动/webhook',status:'运行中'},
    {name:'competitive-analysis-flow',desc:'竞品分析报告',trigger:'手动',status:'运行中'},
    {name:'doc-generation-flow',desc:'文档体系生成',trigger:'手动',status:'运行中'},
    {name:'inception-flow',desc:'立项建议书',trigger:'手动',status:'运行中'}
  ];
  var ft = document.getElementById('difyFlowTable');
  if (ft) {
    var fh = '<thead><tr><th>工作流名称</th><th>功能描述</th><th>触发方式</th><th>状态</th><th>操作</th></tr></thead><tbody>';
    flows.forEach(function(f) {
      fh += '<tr><td style="font-family:monospace;font-size:11.5px">' + f.name + '</td><td>' + f.desc + '</td><td>' + badge('bgr',f.trigger) + '</td><td style="color:#10b981">● ' + f.status + '</td><td><button class="btn btn-s btn-sm" onclick="openDifyEditor(\'' + f.name + '\',\'' + f.desc + '\')">编辑</button></td></tr>';
    });
    fh += '</tbody>';
    ft.innerHTML = fh;
  }
  var mcps = [
    {name:'GitLab MCP',desc:'代码仓库/MR/流水线',connected:true},
    {name:'飞书 MCP',desc:'消息/审批/日历',connected:true},
    {name:'Figma MCP',desc:'设计稿/组件库同步',connected:true},
    {name:'Jira MCP',desc:'Issue/Sprint管理',connected:false},
    {name:'钉钉 MCP',desc:'消息推送/审批流',connected:true}
  ];
  var mt2 = document.getElementById('mcpTable');
  if (mt2) {
    var mh = '<thead><tr><th>MCP名称</th><th>功能描述</th><th>状态</th><th>操作</th></tr></thead><tbody>';
    mcps.forEach(function(m) {
      mh += '<tr><td><strong>' + m.name + '</strong></td><td>' + m.desc + '</td>';
      mh += '<td style="color:' + (m.connected?'#10b981':'var(--g400)') + '">● ' + (m.connected?'已连接':'未连接') + '</td>';
      mh += '<td><button class="btn btn-s btn-sm" onclick="testMCPConn(\'' + m.name + '\',' + m.connected + ')">' + (m.connected?'验证':'连接') + '</button></td></tr>';
    });
    mh += '</tbody>';
    mt2.innerHTML = mh;
  }
  var roles = state.roles;
  var rt = document.getElementById('roleTable');
  if (rt) {
    var rh = '<thead><tr><th>角色</th><th>权限范围</th><th>操作</th></tr></thead><tbody>';
    roles.forEach(function(r) {
      rh += '<tr><td><strong>' + r.role + '</strong></td><td style="font-size:12px">' + r.perms + '</td><td><button class="btn btn-s btn-sm" onclick="openRoleEdit(\'' + r.role + '\',\'' + r.perms + '\')">编辑</button></td></tr>';
    });
    rh += '</tbody>';
    rt.innerHTML = rh;
  }
}

// ===== MANUALS =====
function genProductManual() {
  var stat = document.getElementById('pmStatus');
  var content = document.getElementById('pmContent');
  var actions = document.getElementById('pmActions');
  if (stat) { stat.className = 'b bam'; stat.textContent = '生成中…'; }
  if (content) content.innerHTML = '<div class="ai-gen" style="justify-content:center;padding:20px">🤖 AI生成中 <span class="dots"><span></span><span></span><span></span></span></div>';
  notify('AI正在生成产品手册…', 'info');
  setTimeout(function() {
    if (stat) { stat.className = 'b bg'; stat.textContent = '完整度 82%'; }
    if (content) {
      content.innerHTML = '<div class="prd"><h3>📖 智慧灌溉决策平台 v2.1 产品手册</h3><div style="display:flex;gap:6px;margin-bottom:10px">' + badge('bai','🤖 AI生成') + badge('bg','82%完整') + '</div>' +
        '<h4>第一章：系统概述</h4><p>智慧灌溉决策平台是一款基于AI大模型的精准农业解决方案，通过整合土壤墒情、气象数据和作物模型，为农场主提供科学的灌溉决策支持。</p>' +
        '<h4>第二章：快速开始</h4><p>1. 微信扫码进入小程序 → 2. 注册农场账号 → 3. 添加地块信息 → 4. 绑定传感器设备 → 5. 开始接收灌溉推荐</p>' +
        '<h4>第三章：功能详细说明</h4><ul><li><strong>灌溉推荐</strong>：首页查看今日推荐，点击"执行"一键开启灌溉</li><li><strong>历史记录</strong>：查看历史灌溉数据和效果评估</li><li><strong>离线功能</strong>：无网络时可查看历史数据，恢复联网后自动同步</li></ul>' +
        '<h4>第四章：常见问题 FAQ</h4><ul><li>Q：为何推荐和我实际经验不符？A：建议检查传感器埋深位置是否正确。</li><li>Q：离线数据多久同步？A：联网后30秒内自动同步。</li></ul></div>';
    }
    if (actions) actions.style.display = 'flex';
    notify('产品手册已生成，完整度82%', '');
  }, 2500);
}

function genImplManual() {
  var stat = document.getElementById('imStatus');
  var content = document.getElementById('imContent');
  var mode = getVal('impl-mode') || 'Docker Compose';
  if (stat) { stat.className = 'b bam'; stat.textContent = '生成中…'; }
  notify('AI正在生成实施手册…', 'info');
  setTimeout(function() {
    if (stat) { stat.className = 'b bg'; stat.textContent = '已生成'; }
    if (content) {
      content.innerHTML = '<div class="prd"><h3>🚀 实施手册 · ' + mode + '</h3>' +
        '<h4>一、环境要求</h4><ul><li>服务器：4核8G+（推荐8核16G）</li><li>操作系统：CentOS 7+ / Ubuntu 20.04</li><li>Docker 20.10+，Docker Compose 2.0+</li></ul>' +
        '<h4>二、安装步骤</h4><p><code style="background:var(--g100);padding:2px 6px;border-radius:4px">git clone https://gitee.com/agri/agriplm.git && cd agriplm && cp .env.example .env</code></p>' +
        '<p>编辑 .env 配置后执行：<code style="background:var(--g100);padding:2px 6px;border-radius:4px">docker-compose up -d</code></p>' +
        '<h4>三、初始化数据</h4><p><code style="background:var(--g100);padding:2px 6px;border-radius:4px">agriplm-cli db:migrate && agriplm-cli db:seed</code></p>' +
        '<h4>四、常见安装问题</h4><ul><li>端口冲突：修改.env中SERVER_PORT配置</li><li>数据库连接失败：检查DB_HOST是否可达</li></ul></div>';
    }
    notify('实施手册已生成', '');
  }, 2000);
}

function genOpsManual() {
  var stat = document.getElementById('omStatus');
  var content = document.getElementById('omContent');
  if (stat) { stat.className = 'b bam'; stat.textContent = '生成中…'; }
  notify('AI正在生成运维手册，包含IoT设备运维指南…', 'info');
  setTimeout(function() {
    if (stat) { stat.className = 'b bg'; stat.textContent = '已生成'; }
    if (content) {
      content.innerHTML = '<div class="prd"><h3>🛠️ 运维手册 · 智慧灌溉平台</h3>' +
        '<h4>一、监控指标</h4><ul><li>API响应时间P99（告警阈值：>500ms）</li><li>AI推荐服务CPU占用（告警：>85%）</li><li>MQTT设备在线率（告警：<90%）</li></ul>' +
        '<h4>二、告警规则</h4><ul><li>P0告警：服务宕机 → 立即钉钉+电话通知</li><li>P1告警：响应超时 → 5分钟内通知</li></ul>' +
        '<h4>三、IoT设备运维（农业特有）</h4><ul><li>土壤传感器：季节性校准（每季度一次）</li><li>气象站：每月检查风速仪清洁，防虫巢</li><li>灌溉控制器：冬季防冻，疏通电磁阀</li></ul>' +
        '<h4>四、数据备份</h4><ul><li>每日凌晨2点自动增量备份</li><li>每周日全量备份至OSS</li><li>恢复：<code style="background:var(--g100);padding:2px 5px;border-radius:4px">agriplm-cli db:restore --date=2026-04-10</code></li></ul></div>';
    }
    notify('运维手册已生成，包含农业IoT设备专项运维指南', '');
  }, 2000);
}

// ===== MOCK SERVER =====
var mockServerInterval = null;
var mockReqCount = 0;
var mockCurrentPath = '';

function openMockServer(path) {
  mockCurrentPath = path;
  mockReqCount = 0;
  document.getElementById('mock-url').textContent = 'http://mock.agri.local:9090' + path;
  document.getElementById('mock-status').textContent = '● 运行中';
  document.getElementById('mock-status').style.color = '#10b981';
  document.getElementById('mock-req-count').textContent = '0';
  document.getElementById('mock-log-area').textContent = '[' + nowStr() + '] 🚀 Mock服务启动: http://mock.agri.local:9090' + path + '\n';
  clearInterval(mockServerInterval);
  mockServerInterval = setInterval(function() {
    if (Math.random() > 0.6) simulateMockIncoming();
  }, 3000);
  openModal('modal-mockserver');
  notify('Mock服务已启动：' + path, '');
}

function closeMockServer() {
  clearInterval(mockServerInterval);
  closeModal('modal-mockserver');
}

function stopMockServer() {
  clearInterval(mockServerInterval);
  document.getElementById('mock-status').textContent = '● 已停止';
  document.getElementById('mock-status').style.color = 'var(--g400)';
  appendMockLog('[' + nowStr() + '] ⏹ Mock服务已停止\n');
}

function sendMockRequest() { simulateMockIncoming(true); }

function simulateMockIncoming(manual) {
  var fields = ['350200-FC-0342','410500-FC-1208','530100-FC-0891'];
  var field = fields[Math.floor(Math.random()*fields.length)];
  var latency = 80 + Math.floor(Math.random()*120);
  mockReqCount++;
  document.getElementById('mock-req-count').textContent = mockReqCount;
  appendMockLog('[' + nowStr() + '] → GET ' + mockCurrentPath + '?field_code=' + field + '\n');
  setTimeout(function() {
    var water = (10 + Math.random()*8).toFixed(1);
    var conf = (0.8 + Math.random()*0.18).toFixed(2);
    appendMockLog('[' + nowStr() + '] ← 200 OK (' + latency + 'ms) {"recommend_time":"06:30","water_amount":' + water + ',"confidence":' + conf + '}\n');
  }, latency);
}

function appendMockLog(text) {
  var el = document.getElementById('mock-log-area');
  if (!el) return;
  el.textContent += text;
  el.scrollTop = el.scrollHeight;
}

function clearMockLog() {
  var el = document.getElementById('mock-log-area');
  if (el) el.textContent = '[' + nowStr() + '] 日志已清空\n';
  mockReqCount = 0;
  document.getElementById('mock-req-count').textContent = '0';
}

// ===== DIFY EDITOR =====
function openDifyEditor(name, desc) {
  document.getElementById('dify-modal-title').textContent = 'Dify编辑：' + desc;
  document.getElementById('dify-modal-sub').textContent = name;
  renderDifyCanvas();
  renderDifyVars();
  var modal = document.getElementById('modal-difyedit');
  if (modal) {
    modal.querySelectorAll('.tab').forEach(function(t,i) { t.classList.toggle('active', i===0); });
    modal.querySelectorAll('.tp').forEach(function(p,i) { p.classList.toggle('active', i===0); });
  }
  document.getElementById('dify-test-result').innerHTML = '';
  openModal('modal-difyedit');
}

function renderDifyCanvas() {
  var el = document.getElementById('dify-flow-canvas');
  if (!el) return;
  el.innerHTML = '<svg width="100%" height="200" viewBox="0 0 600 200" xmlns="http://www.w3.org/2000/svg">' +
    '<defs><marker id="arr" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto"><polygon points="0 0,8 3,0 6" fill="#9ca3af"/></marker></defs>' +
    '<line x1="120" y1="100" x2="180" y2="100" stroke="#9ca3af" stroke-width="2" marker-end="url(#arr)"/>' +
    '<line x1="290" y1="100" x2="330" y2="50" stroke="#9ca3af" stroke-width="2" marker-end="url(#arr)"/>' +
    '<line x1="290" y1="100" x2="330" y2="140" stroke="#9ca3af" stroke-width="2" marker-end="url(#arr)"/>' +
    '<line x1="440" y1="50" x2="470" y2="90" stroke="#9ca3af" stroke-width="2" marker-end="url(#arr)"/>' +
    '<line x1="440" y1="140" x2="470" y2="110" stroke="#9ca3af" stroke-width="2" marker-end="url(#arr)"/>' +
    '<rect x="20" y="80" width="100" height="40" rx="8" fill="#eff6ff" stroke="#3b82f6" stroke-width="2"/><text x="70" y="105" text-anchor="middle" font-size="12" fill="#1d4ed8" font-weight="600">📥 输入节点</text>' +
    '<rect x="180" y="80" width="110" height="40" rx="8" fill="#f5f3ff" stroke="#7c3aed" stroke-width="2"/><text x="235" y="97" text-anchor="middle" font-size="11" fill="#5b21b6" font-weight="600">🧠 LLM调用</text><text x="235" y="113" text-anchor="middle" font-size="10" fill="#7c3aed">DeepSeek-V3</text>' +
    '<rect x="330" y="30" width="110" height="38" rx="8" fill="#e8f5ee" stroke="#4caf78" stroke-width="2"/><text x="385" y="46" text-anchor="middle" font-size="11" fill="#1a5235" font-weight="600">📚 AgriKB</text><text x="385" y="62" text-anchor="middle" font-size="10" fill="#2d7a4f">RAG检索</text>' +
    '<rect x="330" y="122" width="110" height="38" rx="8" fill="#fef3c7" stroke="#f59e0b" stroke-width="2"/><text x="385" y="138" text-anchor="middle" font-size="11" fill="#92400e" font-weight="600">🔧 代码执行</text><text x="385" y="154" text-anchor="middle" font-size="10" fill="#92400e">Python</text>' +
    '<rect x="470" y="80" width="100" height="40" rx="8" fill="#dcfce7" stroke="#10b981" stroke-width="2"/><text x="520" y="105" text-anchor="middle" font-size="12" fill="#166534" font-weight="600">📤 输出节点</text>' +
    '</svg>' +
    '<div style="font-size:12px;color:var(--g500);margin-top:8px">节点数：5 · 连线数：5 · 状态：✅ 已部署</div>';
}

function renderDifyVars() {
  var t = document.getElementById('dify-vars-table');
  if (!t) return;
  var vars = [
    {name:'input_text',type:'String',desc:'输入需求文本',req:true},
    {name:'agrikb_context',type:'String',desc:'AgriKB检索结果',req:false},
    {name:'model_id',type:'String',desc:'使用的模型ID',req:true},
    {name:'output_format',type:'Enum',desc:'输出格式(markdown/json)',req:false}
  ];
  var h = '<thead><tr><th>变量名</th><th>类型</th><th>说明</th><th>必填</th><th>操作</th></tr></thead><tbody>';
  vars.forEach(function(v) {
    h += '<tr><td style="font-family:monospace;font-size:12px">' + v.name + '</td><td>' + badge('bbl',v.type) + '</td><td style="font-size:12px">' + v.desc + '</td><td>' + badge(v.req?'brd':'bgr',v.req?'必填':'可选') + '</td><td><button class="btn btn-s btn-sm" onclick="notify(\'编辑变量\',\'info\')">编辑</button></td></tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
}

function switchDifyTab(el, idx) {
  var modal = el.closest('.modal');
  modal.querySelectorAll('.tab').forEach(function(t,i) { t.classList.toggle('active', i===idx); });
  modal.querySelectorAll('.tp').forEach(function(p,i) { p.classList.toggle('active', i===idx); });
}

function saveDifyFlow() { notify('工作流已保存并部署到Dify服务器', ''); closeModal('modal-difyedit'); }
function optimizeDifyFlow() { notify('AI正在分析工作流，建议合并两个并行分支以减少token消耗…', 'info'); }

function runDifyTest() {
  var el = document.getElementById('dify-test-result');
  if (!el) return;
  el.innerHTML = '<div class="ai-gen" style="padding:10px 0">🤖 运行中 <span class="dots"><span></span><span></span><span></span></span></div>';
  setTimeout(function() {
    el.innerHTML = '<div style="background:var(--g50);border-radius:8px;padding:12px"><div style="font-size:12px;font-weight:700;color:#10b981;margin-bottom:6px">✅ 运行成功 · 耗时 1.34s · Token: 847</div><div class="code" style="font-size:11px">{"status":"success","output":"PRD已生成","tokens_used":847}</div></div>';
    notify('Dify工作流测试运行成功', '');
  }, 1800);
}

// ===== ROLE EDIT =====
function openRoleEdit(role, perms) {
  state.currentRole = role;
  document.getElementById('role-modal-title').textContent = '编辑角色权限：' + role;
  var roleDescEl = document.getElementById('role-desc-edit');
  if (roleDescEl) roleDescEl.value = role + '角色负责' + (perms.split('✅')[1] || '相关功能') + '等工作。';
  var modules = ['立项管理','竞品情报','需求管理','PRD生成','UED协同','系统设计','数据库设计','接口设计','研发看板','测试方案','测试用例','测试数据','提测管理','自动化测试','缺陷管理','测试报告','API文档','产品手册','实施手册','运维手册','效能分析','系统设置'];
  var canAll = role === '部门负责人' || role === '项目经理';
  var h = '<div style="font-size:12px;font-weight:700;color:var(--g600);margin-bottom:8px">功能模块权限配置</div>';
  h += '<div style="max-height:280px;overflow-y:auto">';
  h += '<div style="display:grid;grid-template-columns:1fr 60px 60px 60px;gap:4px;padding:5px 8px;background:var(--g50);border-radius:6px;font-size:11px;font-weight:700;color:var(--g500);margin-bottom:4px"><span>模块</span><span style="text-align:center">查看</span><span style="text-align:center">编辑</span><span style="text-align:center">管理</span></div>';
  modules.forEach(function(m) {
    h += '<div style="display:grid;grid-template-columns:1fr 60px 60px 60px;gap:4px;padding:5px 8px;border-radius:6px;font-size:12px">';
    h += '<span>' + m + '</span>';
    h += '<span style="text-align:center"><input type="checkbox" ' + (canAll?'checked':'') + '/></span>';
    h += '<span style="text-align:center"><input type="checkbox" ' + (canAll?'checked':'') + '/></span>';
    h += '<span style="text-align:center"><input type="checkbox" ' + (canAll&&role==='部门负责人'?'checked':'') + '/></span>';
    h += '</div>';
  });
  h += '</div>';
  document.getElementById('role-perm-list').innerHTML = h;
  openModal('modal-roleedit');
}

function saveRolePerms() { closeModal('modal-roleedit'); notify('角色"' + state.currentRole + '"权限已保存', ''); }

// ===== DESIGN PREVIEW =====
function openDesignPreview(version, desc) {
  document.getElementById('dp-title').textContent = '设计稿预览 ' + version;
  document.getElementById('dp-meta').textContent = (desc||'') + ' · 设计师小林 · Figma MCP同步';
  var canvas = document.getElementById('dp-canvas');
  if (!canvas) return;
  canvas.innerHTML = '<svg width="560" height="300" xmlns="http://www.w3.org/2000/svg" style="max-width:100%;border-radius:8px">' +
    '<rect width="560" height="300" fill="#2c2c2c" rx="8"/>' +
    '<rect x="0" y="0" width="560" height="36" fill="#1a1a2e" rx="8"/>' +
    '<circle cx="16" cy="18" r="5" fill="#ff5f57"/><circle cx="30" cy="18" r="5" fill="#febc2e"/><circle cx="44" cy="18" r="5" fill="#28c840"/>' +
    '<text x="280" y="22" text-anchor="middle" fill="#888" font-size="11">智慧灌溉决策平台 v2.1 · ' + (desc||'灌溉控制页面') + '</text>' +
    '<rect x="0" y="36" width="80" height="264" fill="#1e3a5f"/>' +
    '<rect x="88" y="44" width="310" height="100" rx="6" fill="#333"/>' +
    '<text x="108" y="66" fill="#4caf78" font-size="12" font-weight="bold">今日灌溉推荐</text>' +
    '<text x="108" y="84" fill="#aaa" font-size="10">地块：350200-FC-0342 | 作物：水稻（晚稻）</text>' +
    '<rect x="108" y="92" width="120" height="32" rx="6" fill="#2d7a4f"/>' +
    '<text x="168" y="113" text-anchor="middle" fill="white" font-size="11" font-weight="bold">06:30 · 12.5mm</text>' +
    '<rect x="88" y="152" width="310" height="80" rx="6" fill="#333"/>' +
    '<text x="108" y="172" fill="#888" font-size="10">土壤墒情趋势 (7天)</text>' +
    '<polyline points="108,200 148,190 188,205 228,185 268,192 308,178 348,188 388,182" fill="none" stroke="#4caf78" stroke-width="2"/>' +
    '<rect x="408" y="44" width="144" height="248" rx="6" fill="#333"/>' +
    '<text x="428" y="62" fill="#888" font-size="10">传感器状态</text>' +
    '<rect x="416" y="68" width="122" height="40" rx="5" fill="#1e3a5f"/>' +
    '<text x="426" y="84" fill="#4caf78" font-size="10">🌱 土壤传感器</text>' +
    '<text x="426" y="98" fill="#aaa" font-size="9">含水率 23.4% · 正常</text>' +
    '</svg>';
  openModal('modal-designpreview');
}

function syncToFigma() {
  notify('正在通过Figma MCP同步AI修改意见…', 'info');
  setTimeout(function() { notify('2条设计建议已同步至Figma注释，设计师将收到通知', ''); }, 1500);
}

function openFigmaSync() {
  notify('正在通过Figma MCP同步最新设计稿…', 'info');
  setTimeout(function() {
    state.uedVersions.unshift({v:'v2.4',date:dateStr(),author:'设计师小林',status:'最新',desc:'同步灌溉控制最新稿'});
    if (state.uedVersions[1]) state.uedVersions[1].status = '已归档';
    renderUED();
    notify('已同步最新设计稿 v2.4，新增3个灌溉控制组件', '');
  }, 2000);
}

// ===== SPRINT MANAGEMENT =====
function openSprintModal() {
  renderSprintList();
  openModal('modal-sprint');
}

function renderSprintList() {
  var el = document.getElementById('sprint-list');
  if (!el) return;
  var h = '';
  state.sprints.forEach(function(s) {
    var cls = s.status === 'active' ? 'active' : s.status === 'done' ? 'done' : '';
    h += '<div class="sprint-card ' + cls + '">';
    h += '<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px"><span style="font-weight:700;font-size:13px">' + s.name + '</span>' + badge(s.status==='done'?'bg':s.status==='active'?'bbl':'bgr', s.status==='done'?'✅ 已完成':s.status==='active'?'🔄 进行中':'📅 计划中') + '</div>';
    h += '<div style="font-size:12px;color:var(--g600);margin-bottom:5px">目标：' + s.goal + '</div>';
    h += '<div style="font-size:11.5px;color:var(--g500);margin-bottom:6px">开始：' + s.start + ' · ' + s.days + '天</div>';
    if (s.status !== 'planned') {
      h += progressBar(s.progress, 'pfg');
      h += '<div style="font-size:11px;color:var(--g500);margin-top:3px">进度：' + s.progress + '%</div>';
    }
    h += '</div>';
  });
  el.innerHTML = h;
}

function addSprint() {
  var name = document.getElementById('ns-sprint-name').value;
  if (!name) { notify('请输入迭代名称', 'err'); return; }
  state.sprints.push({
    id: 'SP-00' + (state.sprints.length+1),
    name: name,
    start: document.getElementById('ns-sprint-start').value,
    days: parseInt(document.getElementById('ns-sprint-days').value) || 14,
    goal: document.getElementById('ns-sprint-goal').value || '待定',
    status: 'planned',
    progress: 0
  });
  renderSprintList();
  notify('迭代"' + name + '"已创建', '');
}

// ===== TEST ENVIRONMENT =====
function openTestEnvModal() {
  renderTestEnvTable();
  openModal('modal-testenv');
}

function renderTestEnvTable() {
  var t = document.getElementById('testenv-tbl');
  if (!t) return;
  var h = '<thead><tr><th>环境</th><th>域名/IP</th><th>类型</th><th>状态</th><th>负责人</th><th>最后检查</th><th>操作</th></tr></thead><tbody>';
  state.testEnvs.forEach(function(e) {
    h += '<tr><td><strong>' + e.name + '</strong></td><td style="font-family:monospace;font-size:12px">' + e.url + '</td><td>' + badge('bgr',e.type) + '</td>';
    h += '<td><span style="display:inline-flex;align-items:center;gap:4px;font-size:12px;font-weight:500;color:' + (e.status==='online'?'#10b981':'var(--rd)') + '"><span style="width:7px;height:7px;border-radius:50%;background:currentColor;display:inline-block"></span>' + (e.status==='online'?'在线':'离线') + '</span></td>';
    h += '<td>' + e.owner + '</td><td style="font-size:11.5px">' + e.lastCheck + '</td>';
    h += '<td style="white-space:nowrap"><button class="btn btn-s btn-sm" onclick="pingEnv(\'' + e.id + '\')">🔍 探活</button> <button class="btn btn-sm" style="padding:4px 7px;background:var(--rdl);color:var(--rd);border:1px solid #fecaca;border-radius:6px;cursor:pointer" onclick="deleteTestEnv(\'' + e.id + '\')">🗑️</button></td>';
    h += '</tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
}

function pingEnv(id) {
  var e = state.testEnvs.find(function(x) { return x.id === id; });
  if (!e) return;
  notify('正在探活 ' + e.url + '…', 'info');
  setTimeout(function() {
    e.status = Math.random() > 0.2 ? 'online' : 'offline';
    e.lastCheck = new Date().toLocaleString('zh-CN').slice(0,16);
    renderTestEnvTable();
    notify(e.name + ' 探活：' + (e.status==='online'?'✅ 在线':'❌ 离线'), e.status==='online'?'':'err');
  }, 1000);
}

function addTestEnv() {
  var name = document.getElementById('ne-name').value;
  var url = document.getElementById('ne-url').value;
  if (!name || !url) { notify('请填写环境名称和域名', 'err'); return; }
  state.testEnvs.push({
    id: 'ENV-00' + (state.testEnvs.length+1),
    name: name, url: url,
    type: document.getElementById('ne-type').value,
    owner: document.getElementById('ne-owner').value || '待分配',
    status: 'offline',
    lastCheck: '未检查'
  });
  renderTestEnvTable();
  notify('测试环境"' + name + '"已添加', '');
}

function deleteTestEnv(id) {
  var idx = state.testEnvs.findIndex(function(x) { return x.id === id; });
  if (idx > -1) { state.testEnvs.splice(idx, 1); renderTestEnvTable(); notify('环境已删除', 'warn'); }
}

// ===== AGRIKB =====
function renderKBOverview() {
  var cats = [
    {icon:'📜',name:'国家/行业标准',count:342,desc:'NY/T系列农业行业标准'},
    {icon:'🌾',name:'作物栽培技术',count:891,desc:'水稻/小麦/玉米等主要作物'},
    {icon:'🐛',name:'病虫害防治',count:623,desc:'600+种病虫害识别与防治'},
    {icon:'💧',name:'节水灌溉技术',count:287,desc:'灌溉制度/土壤墒情监测'},
    {icon:'🌡️',name:'气象农业数据',count:412,desc:'农业气候区划/气象指标'},
    {icon:'🧪',name:'土壤肥料知识',count:292,desc:'土壤类型/施肥技术规程'}
  ];
  var el = document.getElementById('kb-cats');
  if (!el) return;
  var h = '';
  cats.forEach(function(c) {
    h += '<div class="kb-cat"><div style="font-size:18px;flex-shrink:0">' + c.icon + '</div><div style="flex:1"><div style="font-weight:600;font-size:12.5px">' + c.name + '</div><div style="font-size:11.5px;color:var(--g500)">' + c.desc + '</div></div><div style="font-size:12px;color:var(--g500)">' + c.count + '篇</div><div style="font-size:11px;color:#10b981">✅ 已向量化</div></div>';
  });
  el.innerHTML = h;
  var docsEl = document.getElementById('kb-docs-tbl');
  if (docsEl) {
    var dh = '<thead><tr><th>文档名称</th><th>分类</th><th>大小</th><th>状态</th><th>日期</th><th>操作</th></tr></thead><tbody>';
    state.kbDocs.forEach(function(d) {
      dh += '<tr><td style="font-size:12.5px;font-weight:500">' + d.name + '</td><td>' + badge('bbl',d.cat) + '</td><td style="color:var(--g400)">' + d.size + '</td><td>' + badge('bg',d.status) + '</td><td style="font-size:11.5px;color:var(--g400)">' + d.date + '</td><td><button class="btn btn-s btn-sm" onclick="openKBDoc(\'' + d.id + '\',\'' + d.name.replace(/'/g,"\\'") + '\')">查看</button></td></tr>';
    });
    dh += '</tbody>';
    docsEl.innerHTML = dh;
  }
}

function openKBDoc(id, name) {
  var doc = state.kbDocs.find(function(d) { return d.id === id; });
  if (!doc) return;
  var el = document.getElementById('kb-results');
  if (el) {
    el.innerHTML = '<div style="background:var(--gpale);border-radius:8px;padding:12px;border-left:3px solid var(--gp)">' +
      '<div style="font-weight:700;font-size:13px;margin-bottom:6px">📄 ' + doc.name + '</div>' +
      '<div style="display:flex;gap:6px;margin-bottom:8px">' + badge('bbl',doc.cat) + badge('bgr',doc.size) + badge('bg',doc.status) + '</div>' +
      '<div style="font-size:12px;color:var(--g600);line-height:1.7">已完成向量化处理。最后更新：' + doc.date + ' · 向量维度：1536</div>' +
      '<div class="btn-row mt2"><button class="btn btn-s btn-sm" onclick="notify(\'正在准备下载…\',\'info\')">⬇️ 下载</button><button class="btn btn-rd btn-sm" onclick="deleteKBDoc(\'' + id + '\')">🗑️ 删除</button></div>' +
      '</div>';
    // Switch to search tab
    var modal = document.getElementById('modal-agrikb');
    if (modal) {
      modal.querySelectorAll('.tab').forEach(function(t,i) { t.classList.toggle('active', i===2); });
      modal.querySelectorAll('.tp').forEach(function(p,i) { p.classList.toggle('active', i===2); });
    }
  }
  notify('查看文档：' + doc.name, 'info');
}

function deleteKBDoc(id) {
  var idx = state.kbDocs.findIndex(function(d) { return d.id === id; });
  if (idx > -1) { state.kbDocs.splice(idx, 1); }
  closeModal('modal-agrikb');
  notify('文档已从知识库删除', 'warn');
}

function testKBSearch() {
  var q = document.getElementById('kb-query').value;
  if (!q) { notify('请输入检索词', 'warn'); return; }
  var el = document.getElementById('kb-results');
  if (el) el.innerHTML = '<div class="ai-gen" style="padding:12px 0">🔍 向量检索中 <span class="dots"><span></span><span></span><span></span></span></div>';
  setTimeout(function() {
    var results = [
      {score:0.96,doc:'水稻全生育期灌溉制度研究',snippet:'水稻需水临界期为分蘖期，土壤含水率应维持在75%-85%田间持水量，此阶段缺水将导致减产8%-15%。'},
      {score:0.91,doc:'NY/T 1782-2021 农田灌溉水质标准',snippet:'节水灌溉模式下，旱作物土壤含水率下限控制标准：55%-65%田间持水量。'},
      {score:0.87,doc:'土壤墒情监测技术规范',snippet:'土壤体积含水率（θv）与重量含水率（θm）转换：θv = θm × ρb，ρb为土壤容重。'}
    ];
    if (!el) return;
    var h = '<div style="font-size:12px;color:var(--g500);margin-bottom:8px">检索到 ' + results.length + ' 条相关文档：</div>';
    results.forEach(function(r) {
      h += '<div style="background:var(--g50);border-radius:8px;padding:10px 12px;margin-bottom:7px;border-left:3px solid var(--gp)">';
      h += '<div style="display:flex;justify-content:space-between;margin-bottom:4px"><span style="font-weight:700;font-size:12.5px">' + r.doc + '</span>' + badge('bg','相似度 ' + Math.round(r.score*100) + '%') + '</div>';
      h += '<div style="font-size:12px;color:var(--g600)">' + r.snippet + '</div></div>';
    });
    el.innerHTML = h;
    notify('AgriKB检索完成，找到 ' + results.length + ' 条相关知识', '');
  }, 1200);
}

function switchKBTab(el, idx) {
  var modal = el.closest('.modal');
  modal.querySelectorAll('.tab').forEach(function(t,i) { t.classList.toggle('active', i===idx); });
  modal.querySelectorAll('.tp').forEach(function(p,i) { p.classList.toggle('active', i===idx); });
  if (idx === 0) renderKBOverview();
}

function batchVectorize(btn) {
  btn.disabled = true;
  btn.innerHTML = '<span class="ai-gen">向量化中 <span class="dots"><span></span><span></span><span></span></span></span>';
  setTimeout(function() {
    btn.disabled = false; btn.innerHTML = '✨ 批量向量化';
    notify('文档向量化完成', '');
  }, 3000);
}

function openKBUpload() { openModal('modal-kb-upload'); }
function triggerKBUpload() { document.getElementById('kb-file-input').click(); }
function handleKBDrop(event) { event.preventDefault(); handleKBUpload(event.dataTransfer); }
function handleKBUpload(input) {
  var files = Array.from(input.files || []);
  if (!files.length) return;
  state.kbUploadFiles = files;
  var listEl = document.getElementById('kb-upload-list');
  if (!listEl) return;
  var h = '';
  files.forEach(function(f, i) {
    h += '<div style="display:flex;align-items:center;gap:8px;padding:7px 10px;background:var(--g50);border-radius:7px;margin-bottom:5px" id="kb-file-item-' + i + '">';
    h += '<span style="font-size:18px">' + (f.name.endsWith('.pdf')?'📄':f.name.endsWith('.md')?'📝':'📃') + '</span>';
    h += '<div style="flex:1"><div style="font-size:12.5px;font-weight:600">' + f.name + '</div><div style="font-size:11px;color:var(--g500)">' + (f.size/1024).toFixed(0) + 'KB</div></div>';
    h += badge('bgr','待处理');
    h += '</div>';
  });
  listEl.innerHTML = h;
}
function processKBUpload() {
  if (!state.kbUploadFiles.length) { notify('请先选择要上传的文件', 'warn'); return; }
  var btn = document.getElementById('kb-upload-btn');
  if (btn) { btn.disabled = true; btn.innerHTML = '<span class="ai-gen">处理中 <span class="dots"><span></span><span></span><span></span></span></span>'; }
  var i = 0;
  var interval = setInterval(function() {
    if (i >= state.kbUploadFiles.length) {
      clearInterval(interval);
      if (btn) { btn.disabled = false; btn.innerHTML = '✨ 上传并向量化'; }
      closeModal('modal-kb-upload');
      renderKBOverview();
      notify(state.kbUploadFiles.length + ' 个文档已上传并完成向量化', '');
      state.kbUploadFiles = [];
      return;
    }
    state.kbDocs.push({
      id: 'KB-' + String(state.kbDocs.length+1).padStart(3,'0'),
      name: state.kbUploadFiles[i].name,
      cat: getVal('kb-upload-cat') || '其他',
      size: (state.kbUploadFiles[i].size/1024/1024).toFixed(1) + 'MB',
      status: '已向量化',
      date: dateStr()
    });
    i++;
  }, 800);
}

// ===== MESSAGES =====
function renderMessages(type) {
  var msgs = type === 'unread' ? state.messages.filter(function(m) { return !m.read; }) : state.messages;
  var el = document.getElementById('msg-list');
  if (!el) return;
  if (!msgs.length) { el.innerHTML = '<div style="text-align:center;padding:30px;color:var(--g400)">暂无消息</div>'; return; }
  var h = '';
  msgs.forEach(function(m) {
    h += '<div class="msg-item ' + (m.read?'':'unread') + '" onclick="clickMsg(\'' + m.id + '\')">';
    h += '<div class="msg-title">' + (m.read?'':'🔴 ') + m.title + '</div>';
    h += '<div class="msg-sub">' + m.body + '</div>';
    h += '<div style="font-size:11px;color:var(--g400);margin-top:3px">' + m.time + '</div></div>';
  });
  el.innerHTML = h;
  var badge_count = state.messages.filter(function(m) { return !m.read; }).length;
  var dot = document.getElementById('notif-badge-dot');
  if (dot) dot.style.display = badge_count > 0 ? 'block' : 'none';
}

function clickMsg(id) {
  var msg = state.messages.find(function(m) { return m.id === id; });
  if (!msg) return;
  msg.read = true;
  closeModal('modal-messages');
  if (msg.page) showPage(msg.page);
  renderMessages('all');
}

function filterMsgType(type) { renderMessages(type); }
function markAllMsgRead() {
  state.messages.forEach(function(m) { m.read = true; });
  renderMessages('all');
  notify('全部消息已标为已读', '');
}

// ===== AI CMD =====
function openAICmd() { openModal('modal-aicmd'); setTimeout(function() { var el = document.getElementById('aiCmdInput'); if (el) el.focus(); }, 100); }

function execAICmd(type) {
  closeModal('modal-aicmd');
  var map = {'生成PRD':'prd','竞品分析':'competitive','测试用例':'testcase','测试数据':'testdata','立项建议':'inception','测试报告':'testreport'};
  if (map[type]) { showPage(map[type]); notify('已跳转到' + type, 'info'); }
}

function runAICmd() {
  var v = document.getElementById('aiCmdInput') ? document.getElementById('aiCmdInput').value.toLowerCase() : '';
  closeModal('modal-aicmd');
  if (v.indexOf('prd') >= 0 || v.indexOf('需求') >= 0) showPage('prd');
  else if (v.indexOf('竞品') >= 0) showPage('competitive');
  else if (v.indexOf('用例') >= 0) showPage('testcase');
  else if (v.indexOf('数据') >= 0) showPage('testdata');
  else if (v.indexOf('立项') >= 0) showPage('inception');
  else if (v.indexOf('报告') >= 0) showPage('testreport');
  else if (v.indexOf('手册') >= 0) showPage('productmanual');
  else notify('已理解指令，请使用菜单导航到对应模块', 'info');
}

function dashAiAction() {
  var input = document.getElementById('dashAiInput');
  var v = input ? input.value.toLowerCase() : '';
  if (!v) return;
  if (v.indexOf('prd') >= 0 || v.indexOf('需求') >= 0) { showPage('prd'); notify('已跳转到PRD生成器', 'info'); }
  else if (v.indexOf('竞品') >= 0) { showPage('competitive'); notify('已跳转到竞品情报', 'info'); }
  else if (v.indexOf('测试用例') >= 0 || v.indexOf('用例') >= 0) { showPage('testcase'); notify('已跳转到测试用例', 'info'); }
  else if (v.indexOf('测试数据') >= 0 || v.indexOf('造数') >= 0) { showPage('testdata'); notify('已跳转到数据工厂', 'info'); }
  else if (v.indexOf('立项') >= 0) { showPage('inception'); notify('已跳转到项目立项', 'info'); }
  else if (v.indexOf('手册') >= 0 || v.indexOf('文档') >= 0) { showPage('productmanual'); notify('已跳转到文档中心', 'info'); }
  else notify('AI正在理解您的指令，请使用顶部功能菜单', 'info');
}

// ===== EXPORT FUNCTIONS =====
function copyToClipboard(text, msg) {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text).then(function() { notify(msg || '已复制', ''); });
  } else {
    var ta = document.createElement('textarea');
    ta.value = text; document.body.appendChild(ta);
    ta.select(); document.execCommand('copy');
    document.body.removeChild(ta);
    notify(msg || '已复制', '');
  }
}

function copySQL() {
  var el = document.getElementById('dbSql');
  if (el) copyToClipboard(el.innerText, 'SQL已复制到剪贴板');
}

function exportFile(content, filename, mimeType) {
  var blob = new Blob([content], {type: mimeType || 'text/plain;charset=utf-8'});
  var url = URL.createObjectURL(blob);
  var a = document.createElement('a');
  a.href = url; a.download = filename;
  document.body.appendChild(a); a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
  notify('已下载：' + filename, '');
}

function exportTestReportPDF() {
  var el = document.getElementById('testReportContent');
  if (!el || el.querySelector('[style*="padding:60px"]')) { notify('请先生成测试报告再导出', 'warn'); return; }
  exportFile('<!DOCTYPE html><html><head><meta charset="UTF-8"><title>测试报告</title></head><body style="font-family:sans-serif;padding:30px">' + el.innerHTML + '</body></html>', '测试报告-' + dateStr() + '.html', 'text/html');
}

function exportManualPDF() {
  var el = document.getElementById('pmContent');
  if (!el || el.querySelector('[style*="padding:40px"]')) { notify('请先生成产品手册再导出', 'warn'); return; }
  exportFile('<!DOCTYPE html><html><head><meta charset="UTF-8"><title>产品手册</title></head><body style="font-family:sans-serif;padding:30px">' + el.innerHTML + '</body></html>', '产品手册-v2.1-' + dateStr() + '.html', 'text/html');
}

function exportTestCases() {
  var rows = ['用例ID,标题,类型,优先级,状态,AI生成'];
  state.testcases.forEach(function(tc) {
    rows.push(tc.id + ',"' + tc.title.replace(/"/g, '""') + '",' + tc.type + ',' + tc.pri + ',' + tc.status + ',' + (tc.ai?'是':'否'));
  });
  exportFile(rows.join('\n'), '测试用例-导出-' + dateStr() + '.csv', 'text/csv;charset=utf-8');
}

function exportTestData() {
  var count = document.getElementById('td-count') ? document.getElementById('td-count').value : '1000';
  var table = document.getElementById('td-table') ? document.getElementById('td-table').value.split('（')[0] : 't_data';
  var content = '// AgriPLM 测试数据导出\n// 表名: ' + table + '\n// 数量: ' + count + ' 条\n';
  exportFile(content, table + '-测试数据-' + count + '条.json', 'application/json');
}

function shareTestReport() {
  var id = Math.random().toString(36).slice(2,8).toUpperCase();
  copyToClipboard('https://reports.agri.local/test/' + id, '测试报告分享链接已复制');
}

function generateOnlineLink() {
  var id = Math.random().toString(36).slice(2,8).toUpperCase();
  copyToClipboard('https://docs.agri.local/share/' + id, '在线阅读链接已生成并复制');
}

// ===== SETTINGS ACTIONS =====
function testModelConn(btn) {
  var row = btn.closest('tr');
  var modelName = row ? (row.querySelector('td:first-child strong') || {}).textContent || '模型' : '模型';
  btn.disabled = true; btn.textContent = '测试中…';
  setTimeout(function() {
    var latency = 80 + Math.floor(Math.random()*200);
    var ok = Math.random() > 0.1;
    btn.disabled = false;
    btn.textContent = ok ? ('✅ ' + latency + 'ms') : '❌ 超时';
    btn.style.color = ok ? '#10b981' : 'var(--rd)';
    notify(ok ? (modelName + ' 连接正常，延迟 ' + latency + 'ms') : (modelName + ' 连接超时'), ok ? '' : 'err');
  }, 1500);
}

function saveModelConfig(btn) {
  btn.disabled = true; btn.textContent = '保存中…';
  setTimeout(function() {
    btn.disabled = false; btn.textContent = '💾 保存配置';
    notify('AI模型配置已保存并生效', '');
  }, 800);
}

function testMCPConn(name, connected) {
  notify('正在' + (connected?'验证':'连接') + ' ' + name + '…', 'info');
  setTimeout(function() { notify(name + ' ' + (connected?'连接验证通过':'连接成功'), ''); }, 1200);
}

// ===== COMPETITIVE SUBSCRIBE =====
function openCompSubscribe() {
  var competitors = ['禅道 PMS','LigaAI','PingCode','Jira+Rovo Dev','Teambition'];
  var el = document.getElementById('comp-sub-list');
  if (!el) return;
  var h = '';
  competitors.forEach(function(c, i) {
    h += '<label style="display:flex;align-items:center;gap:8px;padding:7px 10px;background:var(--g50);border-radius:7px;cursor:pointer;font-size:12.5px"><input type="checkbox" ' + (i<3?'checked':'') + ' value="' + c + '"> ' + c + '</label>';
  });
  el.innerHTML = h;
  openModal('modal-comp-subscribe');
}

function confirmCompSubscribe() {
  var checked = Array.from(document.querySelectorAll('#comp-sub-list input:checked')).map(function(cb) { return cb.value; });
  closeModal('modal-comp-subscribe');
  notify('已订阅 ' + checked.length + ' 个竞品的动态推送', '');
}

// ===== SCREENSHOT UPLOAD =====
function openScreenshotUpload() { openModal('modal-screenshot'); }
function triggerFileInput() { var el = document.getElementById('ss-file-input'); if (el) el.click(); }
function handleScreenshotDrop(event) {
  event.preventDefault();
  event.currentTarget.style.borderColor = 'var(--g300)';
  processScreenshots(Array.from(event.dataTransfer.files).filter(function(f) { return f.type.startsWith('image/'); }));
}
function handleScreenshotUpload(input) { processScreenshots(Array.from(input.files)); }
function processScreenshots(files) {
  if (!files.length) return;
  var previews = document.getElementById('ss-previews');
  var aiResult = document.getElementById('ss-ai-result');
  if (previews) {
    var h = '';
    files.forEach(function(f) {
      h += '<div style="display:flex;align-items:center;gap:8px;padding:8px;background:var(--g50);border-radius:7px;margin-bottom:6px"><div style="width:48px;height:36px;background:var(--g200);border-radius:4px;display:flex;align-items:center;justify-content:center;font-size:18px">🖼️</div><div style="flex:1"><div style="font-size:12.5px;font-weight:600">' + f.name + '</div><div style="font-size:11.5px;color:var(--g500)">' + (f.size/1024).toFixed(0) + 'KB</div></div>' + badge('bg','待处理') + '</div>';
    });
    previews.innerHTML = h;
  }
  if (aiResult) aiResult.innerHTML = '<div class="ai-gen" style="padding:10px 0">🤖 AI识别界面元素中 <span class="dots"><span></span><span></span><span></span></span></div>';
  setTimeout(function() {
    if (aiResult) aiResult.innerHTML = '<div style="background:var(--gpale);border-radius:8px;padding:12px;border-left:3px solid var(--gp)"><div style="font-weight:700;font-size:12.5px;margin-bottom:8px">✅ AI识别完成，已生成操作说明</div><div style="font-size:12px;color:var(--g600);line-height:1.7">📱 识别到：灌溉控制主页<br>🔘 识别到UI元素：导航栏、推荐卡片、传感器状态面板、操作按钮<br>✍️ 已生成功能说明文字</div><button class="btn btn-p btn-sm mt2" onclick="insertToManual()">📖 插入产品手册</button></div>';
    notify(files.length + ' 张截图已上传，AI识别完成', '');
  }, 2000);
}
function insertToManual() { closeModal('modal-screenshot'); showPage('productmanual'); notify('截图说明已插入产品手册对应章节', ''); }

// ===== PRD SAVE =====
function savePRD() {
  var titleEl = document.getElementById('prd-title');
  var saveEl = document.getElementById('prd-save-title');
  if (saveEl && titleEl) saveEl.value = titleEl.value;
  openModal('modal-prdsave');
}
function confirmPRDSave() {
  var title = document.getElementById('prd-save-title').value || 'PRD文档';
  var ver = document.getElementById('prd-save-ver').value || 'v1.0';
  var status = document.getElementById('prd-save-status').value;
  state.savedPRDs.push({title:title, ver:ver, status:status, date:dateStr()});
  closeModal('modal-prdsave');
  notify('PRD"' + title + '"已保存到文档库，版本 ' + ver, '');
}

// ===== REQ DETAIL =====
function openReqDetail(reqId) {
  var req = state.requirements.find(function(r) { return r.id === reqId; });
  if (!req) return;
  state.editingReqId = reqId;
  document.getElementById('rdm-title').textContent = req.title;
  document.getElementById('rdm-id').textContent = req.id + ' · ' + req.src;
  document.getElementById('rdm-edit-title').value = req.title;
  document.getElementById('rdm-edit-desc').value = req.desc || ('背景：' + req.src + '提出的' + req.pri + '级需求。');
  setSelectValue('rdm-edit-src', req.src);
  setSelectValue('rdm-edit-pri', req.pri);
  setSelectValue('rdm-edit-status', req.status);
  setSelectValue('rdm-edit-ai', req.ai);
  // Traceability
  var relTasks = state.tasks.slice(0,2);
  var tEl = document.getElementById('rdm-traceability');
  if (tEl) {
    var h = '<div style="font-size:12.5px;font-weight:700;margin-bottom:10px">📌 关联任务</div>';
    relTasks.forEach(function(t) {
      h += '<div class="trace-row">' + badge('bbl',t.id) + '<span style="color:var(--g300)">→</span><span style="flex:1">' + t.title + '</span>' + statusBadge(t.col) + '</div>';
    });
    h += '<div style="font-size:12.5px;font-weight:700;margin:12px 0 8px">📄 关联PRD</div>';
    h += '<div class="trace-row">' + badge('bai','PRD-042') + '<span style="color:var(--g300)">→</span><span style="flex:1">AI灌溉推荐引擎 v1.0</span>' + badge('bg','已确认') + '</div>';
    tEl.innerHTML = h;
  }
  var histEl = document.getElementById('rdm-history');
  if (histEl) {
    histEl.innerHTML = '<div class="tl"><div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">状态变更：待评审→开发中</div><div class="tls">产品经理 · 2026-04-05</div></div></div><div class="tli"><div class="tld done"></div><div class="tlc"><div class="tlt">AI评估：' + req.ai + '</div><div class="tls">AgriAI · 2026-04-01</div></div></div></div>';
  }
  var modal = document.getElementById('modal-reqdetail');
  if (modal) {
    modal.querySelectorAll('.tab').forEach(function(t,i) { t.classList.toggle('active', i===0); });
    modal.querySelectorAll('.tp').forEach(function(p,i) { p.classList.toggle('active', i===0); });
  }
  openModal('modal-reqdetail');
}

function switchRDTab(el, idx) {
  var modal = el.closest('.modal');
  modal.querySelectorAll('.tab').forEach(function(t,i) { t.classList.toggle('active', i===idx); });
  modal.querySelectorAll('.tp').forEach(function(p,i) { p.classList.toggle('active', i===idx); });
}

function saveReqDetail() {
  var req = state.requirements.find(function(r) { return r.id === state.editingReqId; });
  if (!req) return;
  req.title = document.getElementById('rdm-edit-title').value || req.title;
  req.desc = document.getElementById('rdm-edit-desc').value;
  req.src = document.getElementById('rdm-edit-src').value;
  req.pri = document.getElementById('rdm-edit-pri').value;
  req.status = document.getElementById('rdm-edit-status').value;
  req.ai = document.getElementById('rdm-edit-ai').value;
  closeModal('modal-reqdetail');
  renderRequirements();
  notify('需求已保存', '');
}
function aiRegenReq() {
  var vals = ['高价值','中价值','低价值'];
  var val = vals[Math.floor(Math.random()*3)];
  setSelectValue('rdm-edit-ai', val);
  notify('AI重新评估完成：' + val, '');
}
function deleteReqFromDetail() {
  var idx = state.requirements.findIndex(function(r) { return r.id === state.editingReqId; });
  if (idx > -1) state.requirements.splice(idx, 1);
  closeModal('modal-reqdetail');
  renderRequirements();
  notify('需求 ' + state.editingReqId + ' 已删除', 'warn');
}

// ===== HELPERS =====
function setHTML(id, val) { var el = document.getElementById(id); if (el) el.innerHTML = val; }
function getVal(id) { var el = document.getElementById(id); return el ? el.value : ''; }
function setSelectValue(id, val) {
  var el = document.getElementById(id);
  if (!el) return;
  Array.from(el.options).forEach(function(o) { o.selected = o.value === val || o.textContent === val; });
}

// ===== NAV INIT =====
document.addEventListener('DOMContentLoaded', function() {
  // Keyboard shortcut
  document.addEventListener('keydown', function(e) {
    if ((e.metaKey || e.ctrlKey) && e.key === 'k') { e.preventDefault(); openAICmd(); }
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal-bg.open').forEach(function(m) { m.classList.remove('open'); });
    }
  });
  // AI cmd enter
  var cmdInput = document.getElementById('aiCmdInput');
  if (cmdInput) cmdInput.addEventListener('keydown', function(e) { if (e.key === 'Enter') runAICmd(); });
  // Mark active nav item based on current page
  var cur = location.pathname.replace(/.*\//, '').replace('.html','') || 'dashboard';
  document.querySelectorAll('.ni[data-page]').forEach(function(n) {
    n.classList.toggle('active', n.dataset.page === cur);
  });
  var t = document.getElementById('pgTitle');
  if (t && PAGE_TITLES[cur]) t.textContent = PAGE_TITLES[cur];
});


function addDifyVar() { notify('添加变量：请输入变量名和类型', 'info'); }

function runUEDCheckFromModal() {
  closeModal('modal-designpreview');
  showPage('ued');
  setTimeout(function() { runUEDCheck(); }, 300);
}

function switchMTab(el, prefix, idx) {
  var modal = el.closest('.modal');
  if (!modal) return;
  modal.querySelectorAll('.tab').forEach(function(t,i) { t.classList.toggle('active', i===idx); });
  for (var i = 0; i < 5; i++) {
    var pane = document.getElementById(prefix + '-' + i);
    if (pane) pane.style.display = i === idx ? 'block' : 'none';
  }
}

// ================================================================
// DEVOPS + AI OPENSPEC — NEW MODULES
// ================================================================

// ── State additions ───────────────────────────────────────────────
state.pipelines = [
  {id:'PL-001',name:'agriplm-backend',branch:'main',status:'success',stage:'deploy',duration:'4m 32s',trigger:'push',commit:'a3f7c2d',committer:'王工',time:'10分钟前',steps:[{name:'代码检出',status:'success',dur:'8s'},{name:'单元测试',status:'success',dur:'1m 12s'},{name:'SonarQube扫描',status:'success',dur:'45s'},{name:'Docker构建',status:'success',dur:'58s'},{name:'安全扫描(Trivy)',status:'success',dur:'32s'},{name:'推送镜像',status:'success',dur:'18s'},{name:'部署到TEST',status:'success',dur:'39s'}]},
  {id:'PL-002',name:'agriplm-frontend',branch:'feature/iot-dashboard',status:'running',stage:'test',duration:'2m 08s',trigger:'push',commit:'b9e1f4a',committer:'李工',time:'2分钟前',steps:[{name:'代码检出',status:'success',dur:'6s'},{name:'依赖安装',status:'success',dur:'42s'},{name:'单元测试',status:'running',dur:'—'},{name:'E2E测试',status:'pending',dur:'—'},{name:'构建产物',status:'pending',dur:'—'}]},
  {id:'PL-003',name:'agriplm-ai-service',branch:'main',status:'failed',stage:'security',duration:'3m 17s',trigger:'schedule',commit:'c2d8e5b',committer:'陈工',time:'1小时前',steps:[{name:'代码检出',status:'success',dur:'5s'},{name:'单元测试',status:'success',dur:'56s'},{name:'SAST扫描',status:'failed',dur:'1m 22s'},{name:'Docker构建',status:'skipped',dur:'—'}]},
  {id:'PL-004',name:'agriplm-infra',branch:'main',status:'success',stage:'done',duration:'6m 54s',trigger:'manual',commit:'d4a9c7e',committer:'赵工',time:'3小时前',steps:[{name:'Terraform计划',status:'success',dur:'28s'},{name:'安全合规检查',status:'success',dur:'45s'},{name:'基础设施变更',status:'success',dur:'3m 12s'},{name:'烟雾测试',status:'success',dur:'1m 22s'}]},
];

state.releases = [
  {id:'REL-012',name:'v2.1.0',proj:'智慧灌溉决策平台',env:'PROD',status:'已发布',strategy:'蓝绿部署',date:'2026-04-10',deployer:'张总',note:'灌溉AI引擎v2升级，性能提升40%',rollback:false},
  {id:'REL-011',name:'v2.0.3',proj:'农资电商小程序',env:'PROD',status:'已发布',strategy:'金丝雀(10%)',date:'2026-04-05',deployer:'王工',note:'修复购物车并发Bug，灰度放量中',rollback:false},
  {id:'REL-010',name:'v1.3.0',proj:'病虫害AI识别',env:'STAGING',status:'待审批',strategy:'滚动更新',date:'2026-04-15',deployer:'陈工',note:'新增600种识别，模型升级至v3',rollback:false},
  {id:'REL-009',name:'v1.8.1',proj:'农业溯源平台',env:'PROD',status:'已回滚',strategy:'蓝绿部署',date:'2026-04-02',deployer:'赵工',note:'区块链节点同步异常，已回滚',rollback:true},
];

state.aiSpecs = [
  {id:'SPEC-001',name:'灌溉推荐API',version:'v2.1.0',type:'OpenAPI 3.1',status:'已发布',endpoints:6,aiGenerated:true,lastSync:'2026-04-15',desc:'AI驱动的土壤墒情与灌溉决策接口规范'},
  {id:'SPEC-002',name:'传感器数据流',version:'v1.2.0',type:'AsyncAPI 3.0',status:'草稿',endpoints:4,aiGenerated:true,lastSync:'2026-04-14',desc:'IoT传感器实时数据流接口规范（MQTT）'},
  {id:'SPEC-003',name:'AgriAI Agent接口',version:'v1.0.0',type:'AI Function Spec',status:'设计中',endpoints:12,aiGenerated:true,lastSync:'2026-04-13',desc:'AgriAI多Agent协作调用规范'},
  {id:'SPEC-004',name:'农业知识图谱查询',version:'v1.1.0',type:'GraphQL Schema',status:'已发布',endpoints:8,aiGenerated:false,lastSync:'2026-04-10',desc:'AgriKB图谱查询接口，含RAG检索'},
];

state.aiAgents = [
  {id:'AGT-001',name:'需求分析Agent',model:'DeepSeek-V3',tools:['agrikb_search','req_template','prd_generator'],status:'运行中',calls_today:47,success_rate:96,avg_latency:'1.8s',desc:'分析需求文档，引用农业标准，生成结构化PRD'},
  {id:'AGT-002',name:'代码审查Agent',model:'Claude Sonnet 4.6',tools:['gitlab_mr','sonarqube','security_scan'],status:'运行中',calls_today:23,success_rate:91,avg_latency:'3.2s',desc:'审查MR代码质量、安全漏洞、农业业务逻辑'},
  {id:'AGT-003',name:'测试用例Agent',model:'DeepSeek-V3',tools:['req_reader','agrikb_search','testcase_writer'],status:'运行中',calls_today:38,success_rate:94,avg_latency:'2.1s',desc:'基于需求和AgriKB生成覆盖农业场景的测试用例'},
  {id:'AGT-004',name:'发布评审Agent',model:'DeepSeek-R1',tools:['dora_metrics','risk_evaluator','rollback_advisor'],status:'运行中',calls_today:8,success_rate:100,avg_latency:'4.5s',desc:'发布前AI评审：DORA指标/变更风险/回滚方案'},
  {id:'AGT-005',name:'运维巡检Agent',model:'DeepSeek-V3',tools:['prometheus','alert_manager','iot_monitor'],status:'待机',calls_today:0,success_rate:98,avg_latency:'1.2s',desc:'定时巡检IoT设备状态、API健康度、告警处置'},
];

state.doraMetrics = {
  deployFreq: {val:'8.4次/天', trend:'+23%', level:'精英'},
  leadTime: {val:'1.2天', trend:'-31%', level:'精英'},
  mttr: {val:'18分钟', trend:'-44%', level:'精英'},
  cfr: {val:'3.2%', trend:'-18%', level:'高效'},
  history: [
    {month:'2026-01', df:4.2, lt:2.8, mttr:42, cfr:5.8},
    {month:'2026-02', df:5.1, lt:2.3, mttr:35, cfr:4.9},
    {month:'2026-03', df:7.0, lt:1.7, mttr:26, cfr:4.1},
    {month:'2026-04', df:8.4, lt:1.2, mttr:18, cfr:3.2},
  ]
};

state.featureFlags = [
  {id:'FF-001',name:'iot_realtime_sync',desc:'IoT设备实时数据同步（替代轮询）',status:'灰度30%',env:'PROD',owner:'王工',rollout:30},
  {id:'FF-002',name:'ai_irrigation_v3',desc:'灌溉推荐算法v3（多作物参数支持）',status:'开启',env:'PROD',owner:'陈工',rollout:100},
  {id:'FF-003',name:'blockchain_trace_v2',desc:'溯源区块链v2（性能优化版）',status:'关闭',env:'PROD',owner:'赵工',rollout:0},
  {id:'FF-004',name:'pest_detection_v3',desc:'病虫害AI识别v3（600种）',status:'灰度10%',env:'PROD',owner:'李工',rollout:10},
  {id:'FF-005',name:'offline_sync_delta',desc:'离线增量同步（减少80%数据传输）',status:'开启',env:'TEST',owner:'王工',rollout:100},
];

// ── PAGE TITLES additions ────────────────────────────────────────
PAGE_TITLES['pipeline'] = 'CI/CD 流水线';
PAGE_TITLES['release'] = '发布管理';
PAGE_TITLES['aispec'] = 'AI OpenSpec';
PAGE_TITLES['aiagents'] = 'AI Agent 编排';
PAGE_TITLES['featureflag'] = 'Feature Flag';
PAGE_TITLES['devops'] = 'DevOps 效能';

// ── RENDER FUNCTIONS ─────────────────────────────────────────────

function renderPipeline() {
  var t = document.getElementById('pipelineTable');
  if (!t) return;
  var statusIcon = {success:'✅',running:'🔄',failed:'❌',pending:'⏳'};
  var statusCls = {success:'bg',running:'bbl',failed:'brd',pending:'bgr'};
  var h = '<thead><tr><th>流水线</th><th>分支</th><th>状态</th><th>阶段</th><th>耗时</th><th>触发</th><th>提交</th><th>时间</th><th>操作</th></tr></thead><tbody>';
  state.pipelines.forEach(function(p) {
    h += '<tr onclick="openPipelineDetail(\'' + p.id + '\')" style="cursor:pointer">';
    h += '<td><strong>' + p.name + '</strong></td>';
    h += '<td><code style="font-size:11px;background:var(--g100);padding:2px 6px;border-radius:4px">' + p.branch + '</code></td>';
    h += '<td>' + badge(statusCls[p.status], statusIcon[p.status] + ' ' + p.status) + '</td>';
    h += '<td style="font-size:12px;color:var(--g500)">' + p.stage + '</td>';
    h += '<td style="font-size:12px;font-family:monospace">' + p.duration + '</td>';
    h += '<td>' + badge('bgr', p.trigger) + '</td>';
    h += '<td><code style="font-size:11px;color:var(--pu)">' + p.commit + '</code> <span style="font-size:11.5px;color:var(--g500)">' + p.committer + '</span></td>';
    h += '<td style="font-size:11.5px;color:var(--g400)">' + p.time + '</td>';
    h += '<td><button class="btn btn-s btn-sm" onclick="event.stopPropagation();openPipelineDetail(\'' + p.id + '\')">详情</button>';
    if (p.status === 'failed') h += ' <button class="btn btn-p btn-sm" onclick="event.stopPropagation();retryPipeline(\'' + p.id + '\')">重试</button>';
    h += '</td></tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
  renderPipelineStats();
}

function renderPipelineStats() {
  var total = state.pipelines.length;
  var ok = state.pipelines.filter(function(p){return p.status==='success';}).length;
  var running = state.pipelines.filter(function(p){return p.status==='running';}).length;
  var failed = state.pipelines.filter(function(p){return p.status==='failed';}).length;
  setHTML('pl-total', total);
  setHTML('pl-ok', ok);
  setHTML('pl-running', running);
  setHTML('pl-failed', failed);
  setHTML('pl-rate', Math.round(ok/(total-running)*100) + '%');
}

function openPipelineDetail(id) {
  var p = state.pipelines.find(function(x){return x.id===id;});
  if (!p) return;
  var el = document.getElementById('pipeline-detail-content');
  if (!el) return;
  var stepColors = {success:'#10b981',running:'var(--bl)',failed:'var(--rd)',pending:'var(--g300)',skipped:'var(--g400)'};
  var stepIcons = {success:'✅',running:'🔄',failed:'❌',pending:'⏳',skipped:'⏭'};
  var h = '<div style="display:flex;gap:8px;margin-bottom:14px;flex-wrap:wrap">';
  h += badge({success:'bg',running:'bbl',failed:'brd',pending:'bgr'}[p.status] || 'bgr', p.status);
  h += badge('bgr', p.trigger); h += badge('bgr', p.branch);
  h += '</div>';
  h += '<div style="font-size:12px;color:var(--g500);margin-bottom:14px">提交: <code>' + p.commit + '</code>  ·  ' + p.committer + '  ·  ' + p.time + '  ·  耗时 ' + p.duration + '</div>';
  h += '<div style="font-size:12.5px;font-weight:700;margin-bottom:10px">流水线步骤</div>';
  p.steps.forEach(function(s, i) {
    var color = stepColors[s.status] || 'var(--g400)';
    var icon = stepIcons[s.status] || '⏹';
    var isRunning = s.status === 'running';
    h += '<div style="display:flex;align-items:center;gap:10px;padding:8px 12px;background:var(--g50);border-radius:7px;margin-bottom:5px;border-left:3px solid ' + color + '">';
    h += '<span style="font-size:14px">' + icon + '</span>';
    h += '<span style="flex:1;font-size:12.5px;font-weight:500">' + s.name + '</span>';
    if (isRunning) h += '<span class="ai-gen" style="font-size:11px">执行中 <span class="dots"><span></span><span></span><span></span></span></span>';
    else h += '<span style="font-size:11.5px;font-family:monospace;color:var(--g400)">' + s.dur + '</span>';
    if (s.status === 'failed') h += ' <button class="btn btn-rd btn-sm" onclick="notify(\'查看失败日志: ' + s.name + '\',\'err\')">查看日志</button>';
    h += '</div>';
  });
  el.innerHTML = h;
  openModal('modal-pipeline-detail');
}

function retryPipeline(id) {
  var p = state.pipelines.find(function(x){return x.id===id;});
  if (!p) return;
  p.status = 'running'; p.stage = 'test'; p.time = '刚刚';
  renderPipeline();
  notify('流水线 ' + p.name + ' 已重新触发', 'info');
  setTimeout(function() {
    p.status = 'success'; p.stage = 'done'; p.time = '1分钟前';
    renderPipeline();
    notify(p.name + ' 流水线执行成功 ✅', '');
  }, 3000);
}

function triggerPipeline() {
  var sel = document.getElementById('pl-trigger-repo');
  var name = sel ? sel.value : 'agriplm-backend';
  state.pipelines.unshift({
    id:'PL-00' + (state.pipelines.length+1), name:name,
    branch:'main', status:'running', stage:'build',
    duration:'0m 00s', trigger:'manual', commit:Math.random().toString(36).slice(2,9),
    committer:'张总', time:'刚刚',
    steps:[{name:'代码检出',status:'running',dur:'—'},{name:'单元测试',status:'pending',dur:'—'},{name:'Docker构建',status:'pending',dur:'—'},{name:'部署',status:'pending',dur:'—'}]
  });
  renderPipeline();
  notify('流水线已触发: ' + name, 'info');
}

function aiAnalyzePipeline() {
  notify('AI正在分析流水线健康状态…', 'info');
  setTimeout(function() {
    notify('⚠️ 发现1个安全风险: ai-service SAST扫描检出1个高危漏洞（SQL注入），建议立即修复', 'warn');
  }, 2000);
}

// ── Release Management ────────────────────────────────────────────
function renderRelease() {
  var t = document.getElementById('releaseTable');
  if (!t) return;
  var statusCls = {'已发布':'bg','待审批':'bam','已回滚':'brd','部署中':'bbl'};
  var h = '<thead><tr><th>版本</th><th>项目</th><th>环境</th><th>策略</th><th>状态</th><th>发布人</th><th>日期</th><th>操作</th></tr></thead><tbody>';
  state.releases.forEach(function(r) {
    h += '<tr>';
    h += '<td><strong style="font-family:monospace">' + r.name + '</strong></td>';
    h += '<td>' + r.proj + '</td>';
    h += '<td>' + badge(r.env==='PROD'?'brd':r.env==='STAGING'?'bam':'bbl', r.env) + '</td>';
    h += '<td>' + badge('bgr', r.strategy) + '</td>';
    h += '<td>' + badge(statusCls[r.status]||'bgr', r.status) + '</td>';
    h += '<td>' + r.deployer + '</td>';
    h += '<td style="font-size:11.5px">' + r.date + '</td>';
    h += '<td style="white-space:nowrap">';
    if (r.status === '待审批') {
      h += '<button class="btn btn-p btn-sm" onclick="approveRelease(\'' + r.id + '\')">✅ 批准</button> ';
      h += '<button class="btn btn-rd btn-sm" onclick="rejectRelease(\'' + r.id + '\')">❌ 驳回</button>';
    } else if (r.status === '已发布' && !r.rollback) {
      h += '<button class="btn btn-s btn-sm" onclick="rollbackRelease(\'' + r.id + '\')">↩ 回滚</button>';
    } else {
      h += '<button class="btn btn-s btn-sm" onclick="viewReleaseDetail(\'' + r.id + '\')">详情</button>';
    }
    h += '</td></tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
  renderDoraMetrics();
}

function renderDoraMetrics() {
  var d = state.doraMetrics;
  var levelCls = {'精英':'bg','高效':'bbl','中等':'bam','低效':'brd'};
  var fields = [
    {id:'dora-df', label:'部署频率', ...d.deployFreq},
    {id:'dora-lt', label:'变更前置时间', ...d.leadTime},
    {id:'dora-mttr', label:'平均恢复时间(MTTR)', ...d.mttr},
    {id:'dora-cfr', label:'变更失败率(CFR)', ...d.cfr},
  ];
  fields.forEach(function(f) {
    var el = document.getElementById(f.id);
    if (!el) return;
    el.innerHTML = '<div class="sl">' + f.label + '</div>' +
      '<div class="sv" style="font-size:22px;color:var(--gp)">' + f.val + '</div>' +
      '<div style="display:flex;align-items:center;gap:6px;margin-top:4px">' +
      '<span style="font-size:12px;color:#10b981;font-weight:600">' + f.trend + '</span>' +
      badge(levelCls[f.level]||'bgr', f.level) + '</div>';
  });
}

function approveRelease(id) {
  var r = state.releases.find(function(x){return x.id===id;});
  if (!r) return;
  r.status = '部署中';
  renderRelease();
  notify('发布 ' + r.name + ' 已批准，正在执行' + r.strategy + '…', 'info');
  setTimeout(function() {
    r.status = '已发布';
    renderRelease();
    notify(r.name + ' 发布成功 🎉', '');
  }, 3000);
}

function rejectRelease(id) {
  var r = state.releases.find(function(x){return x.id===id;});
  if (r) { r.status = '已驳回'; renderRelease(); notify('发布 ' + r.name + ' 已驳回', 'warn'); }
}

function rollbackRelease(id) {
  var r = state.releases.find(function(x){return x.id===id;});
  if (!r) return;
  notify('正在执行回滚: ' + r.name + ' → 上个稳定版本…', 'warn');
  setTimeout(function() {
    r.status = '已回滚'; r.rollback = true;
    renderRelease();
    notify(r.name + ' 已成功回滚 ↩', 'warn');
  }, 2500);
}

function viewReleaseDetail(id) {
  var r = state.releases.find(function(x){return x.id===id;});
  if (r) notify('发布详情: ' + r.name + ' · ' + r.note, 'info');
}

function aiReleaseReview() {
  notify('AI Release Agent 正在评审发布风险…', 'info');
  setTimeout(function() {
    notify('📊 AI评审结果: 风险等级中等。建议在非业务高峰期（凌晨2-4点）发布，已预置回滚脚本', 'warn');
  }, 2500);
}

function createRelease() {
  var name = document.getElementById('nr-version') ? document.getElementById('nr-version').value : 'v2.2.0';
  if (!name) { notify('请填写版本号', 'err'); return; }
  state.releases.unshift({
    id: 'REL-0' + (13+state.releases.length),
    name: name,
    proj: document.getElementById('nr-proj') ? document.getElementById('nr-proj').value : '智慧灌溉决策平台',
    env: document.getElementById('nr-env') ? document.getElementById('nr-env').value : 'STAGING',
    status: '待审批',
    strategy: document.getElementById('nr-strategy') ? document.getElementById('nr-strategy').value : '蓝绿部署',
    date: dateStr(),
    deployer: '张总',
    note: document.getElementById('nr-note') ? document.getElementById('nr-note').value : '',
    rollback: false
  });
  closeModal('modal-newrelease');
  renderRelease();
  notify('发布单 ' + name + ' 已创建，等待审批', '');
}

// ── AI OpenSpec ───────────────────────────────────────────────────
function renderAISpec() {
  var t = document.getElementById('aispecTable');
  if (!t) return;
  var typeCls = {'OpenAPI 3.1':'bbl','AsyncAPI 3.0':'bpu','AI Function Spec':'bai','GraphQL Schema':'bam'};
  var h = '<thead><tr><th>规范名称</th><th>版本</th><th>类型</th><th>端点数</th><th>状态</th><th>AI生成</th><th>最后同步</th><th>操作</th></tr></thead><tbody>';
  state.aiSpecs.forEach(function(s) {
    h += '<tr>';
    h += '<td><strong>' + s.name + '</strong><br><span style="font-size:11px;color:var(--g500)">' + s.desc + '</span></td>';
    h += '<td style="font-family:monospace;font-size:12px">' + s.version + '</td>';
    h += '<td>' + badge(typeCls[s.type]||'bgr', s.type) + '</td>';
    h += '<td style="text-align:center;font-weight:700;color:var(--gp)">' + s.endpoints + '</td>';
    h += '<td>' + badge(s.status==='已发布'?'bg':s.status==='草稿'?'bgr':'bam', s.status) + '</td>';
    h += '<td style="text-align:center">' + (s.aiGenerated ? badge('bai','🤖 AI') : badge('bgr','手写')) + '</td>';
    h += '<td style="font-size:11.5px;color:var(--g400)">' + s.lastSync + '</td>';
    h += '<td style="white-space:nowrap"><button class="btn btn-s btn-sm" onclick="viewAISpec(\'' + s.id + '\')">查看</button> <button class="btn btn-ai btn-sm" onclick="genAISpec(\'' + s.id + '\')">✨ 更新</button></td>';
    h += '</tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
}

function viewAISpec(id) {
  var s = state.aiSpecs.find(function(x){return x.id===id;});
  if (!s) return;
  var el = document.getElementById('aispec-preview');
  if (!el) return;
  var samples = {
    'OpenAPI 3.1': 'openapi: 3.1.0\ninfo:\n  title: ' + s.name + '\n  version: ' + s.version + '\npaths:\n  /api/v1/irrigation/recommend:\n    get:\n      summary: 获取AI灌溉推荐\n      x-ai-generated: true\n      x-agrikb-ref: "NY/T 1782-2021"\n      parameters:\n        - name: field_code\n          in: query\n          required: true\n          schema:\n            type: string\n            pattern: "^\\\\d{6}-FC-\\\\d{4}$"\n      responses:\n        "200":\n          content:\n            application/json:\n              schema:\n                $ref: "#/components/schemas/IrrigationRecommendation"\n  components:\n    schemas:\n      IrrigationRecommendation:\n        x-ai-confidence: 0.94\n        properties:\n          recommend_time: {type: string}\n          water_amount: {type: number, minimum: 0, maximum: 100}',
    'AsyncAPI 3.0': 'asyncapi: 3.0.0\ninfo:\n  title: ' + s.name + '\n  version: ' + s.version + '\nchannels:\n  soil/sensor/{field_code}/data:\n    address: soil/sensor/{field_code}/data\n    messages:\n      SoilSensorData:\n        payload:\n          x-ai-generated: true\n          type: object\n          properties:\n            moisture_pct:\n              type: number\n              x-agrikb-constraint: "0-45%正常范围"\n            temperature:\n              type: number\n            record_time:\n              type: string\n              format: date-time',
    'AI Function Spec': 'name: agri_irrigation_advisor\ndescription: 农业AI灌溉顾问，基于土壤墒情、气象数据和AgriKB提供灌溉决策\nversion: ' + s.version + '\nparameters:\n  type: object\n  properties:\n    field_code:\n      type: string\n      description: 地块编号(格式: 省市代码-FC-序号)\n    crop_type:\n      type: string\n      description: 作物类型\n      enum: [水稻, 小麦, 玉米, 大豆]\n    soil_moisture:\n      type: number\n      description: 当前土壤含水率(%)\nexecution:\n  model: deepseek-v3\n  tools: [agrikb_search, weather_api, soil_sensor]\n  max_tokens: 2048\n  temperature: 0.2',
    'GraphQL Schema': 'type Query {\n  # 作物知识查询\n  cropKnowledge(name: String!, stage: GrowthStage): CropInfo\n  # 病虫害识别\n  pestDetection(imageBase64: String!): [PestResult]\n  # 灌溉推荐\n  irrigationAdvice(fieldCode: String!): IrrigationPlan\n}\n\ntype CropInfo @aiEnhanced(model: "deepseek-v3") {\n  name: String!\n  kc_coefficient: Float # 来自AgriKB: FAO-56标准\n  irrigation_threshold: IrrigationThreshold\n  standard_ref: String # NY/T行业标准引用\n}'
  };
  var sample = samples[s.type] || 'spec content...';
  el.innerHTML = '<div style="display:flex;gap:8px;margin-bottom:10px">' +
    badge({success:'bg',草稿:'bgr',设计中:'bam'}[s.status]||'bgr', s.status) +
    badge(typeCls[s.type]||'bgr', s.type) +
    (s.aiGenerated ? badge('bai','🤖 AI生成') : '') +
    '</div>' +
    '<div style="font-size:12px;color:var(--g500);margin-bottom:10px">' + s.desc + '  ·  端点数: <strong>' + s.endpoints + '</strong>  ·  最后同步: ' + s.lastSync + '</div>' +
    '<div class="code" style="font-size:11px;max-height:320px;overflow-y:auto;white-space:pre">' + sample + '</div>' +
    '<div class="btn-row mt3">' +
    '<button class="btn btn-p btn-sm" onclick="copyToClipboard(\'' + s.name + ' spec\',\'规范已复制到剪贴板\')">📋 复制</button>' +
    '<button class="btn btn-ai btn-sm" onclick="genAISpec(\'' + s.id + '\')">✨ AI更新</button>' +
    '<button class="btn btn-s btn-sm" onclick="exportFile(\'spec content\',\'' + s.name.replace(/ /g,'_') + '.yaml\',\'text/yaml\')">⬇️ 导出YAML</button>' +
    '</div>';
  var typeCls2 = {'OpenAPI 3.1':'bbl','AsyncAPI 3.0':'bpu','AI Function Spec':'bai','GraphQL Schema':'bam'};
  el.innerHTML = el.innerHTML; // already set
}

var typeCls = {'OpenAPI 3.1':'bbl','AsyncAPI 3.0':'bpu','AI Function Spec':'bai','GraphQL Schema':'bam'};

function genAISpec(id) {
  var s = state.aiSpecs.find(function(x){return x.id===id;});
  if (!s) return;
  notify('AI正在根据代码注释和AgriKB生成' + s.type + '规范…', 'info');
  setTimeout(function() {
    s.lastSync = dateStr();
    s.aiGenerated = true;
    renderAISpec();
    viewAISpec(id);
    notify(s.name + ' 规范已更新，引用AgriKB标准 3 条', '');
  }, 2500);
}

function genNewSpec() {
  var typeEl = document.getElementById('ns-type');
  var nameEl = document.getElementById('ns-name');
  if (!nameEl || !nameEl.value) { notify('请填写规范名称', 'err'); return; }
  var t = typeEl ? typeEl.value : 'OpenAPI 3.1';
  state.aiSpecs.unshift({
    id: 'SPEC-00' + (state.aiSpecs.length+1),
    name: nameEl.value,
    version: 'v1.0.0',
    type: t,
    status: '草稿',
    endpoints: 0,
    aiGenerated: true,
    lastSync: dateStr(),
    desc: document.getElementById('ns-desc') ? document.getElementById('ns-desc').value : ''
  });
  closeModal('modal-newspec');
  renderAISpec();
  notify('规范"' + nameEl.value + '"已创建，AI正在生成初始版本…', 'info');
  setTimeout(function() {
    state.aiSpecs[0].endpoints = 4 + Math.floor(Math.random()*6);
    state.aiSpecs[0].status = '草稿';
    renderAISpec();
    notify('规范初始版本已生成', '');
  }, 2000);
}

// ── AI Agents ─────────────────────────────────────────────────────
function renderAIAgents() {
  var el = document.getElementById('agentGrid');
  if (!el) return;
  var h = '';
  state.aiAgents.forEach(function(a) {
    var statusColor = a.status === '运行中' ? '#10b981' : 'var(--g400)';
    h += '<div class="card" style="border-top:3px solid var(--pu)">';
    h += '<div style="display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:10px">';
    h += '<div><div style="font-weight:700;font-size:13.5px;margin-bottom:3px">' + a.name + '</div>';
    h += '<div style="font-size:11.5px;color:var(--g500)">' + a.desc + '</div></div>';
    h += '<span style="display:inline-flex;align-items:center;gap:4px;font-size:11.5px;font-weight:600;color:' + statusColor + ';white-space:nowrap"><span style="width:7px;height:7px;border-radius:50%;background:currentColor;display:inline-block"></span>' + a.status + '</span>';
    h += '</div>';
    h += '<div style="display:flex;gap:6px;flex-wrap:wrap;margin-bottom:10px">';
    h += badge('bbl', a.model);
    a.tools.forEach(function(t) { h += badge('bgr', t); });
    h += '</div>';
    h += '<div class="g3" style="margin-bottom:10px">';
    h += '<div style="text-align:center"><div style="font-size:18px;font-weight:800;color:var(--pu)">' + a.calls_today + '</div><div style="font-size:10.5px;color:var(--g500)">今日调用</div></div>';
    h += '<div style="text-align:center"><div style="font-size:18px;font-weight:800;color:#10b981">' + a.success_rate + '%</div><div style="font-size:10.5px;color:var(--g500)">成功率</div></div>';
    h += '<div style="text-align:center"><div style="font-size:18px;font-weight:800;color:var(--am)">' + a.avg_latency + '</div><div style="font-size:10.5px;color:var(--g500)">均响应</div></div>';
    h += '</div>';
    h += '<div class="btn-row">';
    if (a.status === '运行中') {
      h += '<button class="btn btn-s btn-sm" onclick="pauseAgent(\'' + a.id + '\')">⏸ 暂停</button>';
    } else {
      h += '<button class="btn btn-p btn-sm" onclick="startAgent(\'' + a.id + '\')">▶ 启动</button>';
    }
    h += '<button class="btn btn-ai btn-sm" onclick="testAgent(\'' + a.id + '\')">🧪 测试</button>';
    h += '<button class="btn btn-s btn-sm" onclick="editAgent(\'' + a.id + '\')">⚙️ 配置</button>';
    h += '</div></div>';
  });
  el.innerHTML = h;
  renderAgentMetrics();
}

function renderAgentMetrics() {
  var totalCalls = state.aiAgents.reduce(function(s,a){return s+a.calls_today;}, 0);
  var running = state.aiAgents.filter(function(a){return a.status==='运行中';}).length;
  var avgSuccess = Math.round(state.aiAgents.reduce(function(s,a){return s+a.success_rate;},0)/state.aiAgents.length);
  setHTML('agent-total-calls', totalCalls);
  setHTML('agent-running', running);
  setHTML('agent-success', avgSuccess + '%');
}

function pauseAgent(id) {
  var a = state.aiAgents.find(function(x){return x.id===id;});
  if (a) { a.status = '待机'; renderAIAgents(); notify('Agent "' + a.name + '" 已暂停', 'warn'); }
}
function startAgent(id) {
  var a = state.aiAgents.find(function(x){return x.id===id;});
  if (a) { a.status = '运行中'; renderAIAgents(); notify('Agent "' + a.name + '" 已启动', ''); }
}
function testAgent(id) {
  var a = state.aiAgents.find(function(x){return x.id===id;});
  if (!a) return;
  notify('正在测试 ' + a.name + '…', 'info');
  setTimeout(function() {
    a.calls_today++;
    renderAIAgents();
    notify(a.name + ' 测试成功 ✅ · 响应: ' + a.avg_latency, '');
  }, 2000);
}
function editAgent(id) {
  var a = state.aiAgents.find(function(x){return x.id===id;});
  if (a) notify('打开 ' + a.name + ' 配置面板 (Dify工作流联动)', 'info');
}

// ── Feature Flags ────────────────────────────────────────────────
function renderFeatureFlag() {
  var t = document.getElementById('ffTable');
  if (!t) return;
  var envCls = {PROD:'brd',TEST:'bbl',STAGING:'bam',DEV:'bgr'};
  var h = '<thead><tr><th>Flag名称</th><th>描述</th><th>环境</th><th>状态/灰度</th><th>负责人</th><th>操作</th></tr></thead><tbody>';
  state.featureFlags.forEach(function(f) {
    h += '<tr>';
    h += '<td><code style="font-size:12px;color:var(--pu)">' + f.name + '</code></td>';
    h += '<td style="font-size:12.5px">' + f.desc + '</td>';
    h += '<td>' + badge(envCls[f.env]||'bgr', f.env) + '</td>';
    h += '<td>';
    if (f.rollout === 100) h += badge('bg', '✅ 全量开启');
    else if (f.rollout === 0) h += badge('bgr', '⭕ 已关闭');
    else h += badge('bam', '🔄 灰度 ' + f.rollout + '%');
    h += '<br><div class="pb" style="height:4px;width:80px;margin-top:4px"><div class="pf ' + (f.rollout===100?'pfg':f.rollout===0?'':'pfam') + '" style="width:' + f.rollout + '%"></div></div>';
    h += '</td>';
    h += '<td>' + f.owner + '</td>';
    h += '<td style="white-space:nowrap">';
    h += '<button class="btn btn-s btn-sm" onclick="toggleFF(\'' + f.id + '\')">' + (f.rollout===100?'关闭':'全量开启') + '</button> ';
    h += '<button class="btn btn-ai btn-sm" onclick="editFFRollout(\'' + f.id + '\')">灰度</button>';
    h += '</td></tr>';
  });
  h += '</tbody>';
  t.innerHTML = h;
}

function toggleFF(id) {
  var f = state.featureFlags.find(function(x){return x.id===id;});
  if (!f) return;
  f.rollout = f.rollout === 100 ? 0 : 100;
  f.status = f.rollout === 100 ? '开启' : '关闭';
  renderFeatureFlag();
  notify('Feature Flag "' + f.name + '" 已' + (f.rollout===100?'全量开启':'关闭'), f.rollout===100?'':'warn');
}

function editFFRollout(id) {
  var f = state.featureFlags.find(function(x){return x.id===id;});
  if (!f) return;
  var pct = prompt('设置灰度比例(0-100):', f.rollout);
  if (pct === null) return;
  var val = Math.min(100, Math.max(0, parseInt(pct)||0));
  f.rollout = val;
  f.status = val===100?'开启':val===0?'关闭':('灰度'+val+'%');
  renderFeatureFlag();
  notify('Feature Flag "' + f.name + '" 灰度已设为 ' + val + '%', 'info');
}

function addFF() {
  var name = document.getElementById('nff-name') ? document.getElementById('nff-name').value : '';
  if (!name) { notify('请输入Flag名称', 'err'); return; }
  state.featureFlags.push({
    id: 'FF-00' + (state.featureFlags.length+1),
    name: name,
    desc: document.getElementById('nff-desc') ? document.getElementById('nff-desc').value : '',
    status: '关闭', env: document.getElementById('nff-env') ? document.getElementById('nff-env').value : 'TEST',
    owner: '张总', rollout: 0
  });
  closeModal('modal-newff');
  renderFeatureFlag();
  notify('Feature Flag "' + name + '" 已创建', '');
}

// ── DevOps Analytics ──────────────────────────────────────────────
function renderDevOpsAnalytics() {
  renderDoraChart();
  renderDeployFreqChart();
  renderLeadTimeBreakdown();
}

function renderDoraChart() {
  var el = document.getElementById('dora-trend-chart');
  if (!el) return;
  var history = state.doraMetrics.history;
  var h = '<div style="font-size:12px;font-weight:700;margin-bottom:8px;color:var(--g600)">DORA 指标趋势（近4个月）</div>';
  h += '<div style="display:grid;grid-template-columns:repeat(4,1fr);gap:6px;margin-bottom:10px">';
  var metrics = [
    {key:'df',label:'部署频率(次/天)',color:'var(--gp)',max:10},
    {key:'lt',label:'前置时间(天)',color:'var(--bl)',max:4,invert:true},
    {key:'mttr',label:'MTTR(分钟)',color:'var(--am)',max:50,invert:true},
    {key:'cfr',label:'变更失败率(%)',color:'var(--rd)',max:8,invert:true},
  ];
  metrics.forEach(function(m) {
    h += '<div style="background:var(--g50);border-radius:7px;padding:8px">';
    h += '<div style="font-size:10.5px;color:var(--g500);margin-bottom:5px">' + m.label + '</div>';
    h += '<div style="display:flex;gap:2px;align-items:flex-end;height:45px">';
    history.forEach(function(row) {
      var val = row[m.key];
      var pct = m.invert ? ((m.max - val) / m.max * 100) : (val / m.max * 100);
      pct = Math.max(5, Math.min(100, pct));
      h += '<div style="flex:1;background:' + m.color + ';border-radius:2px 2px 0 0;height:' + pct + '%;opacity:0.8;position:relative" title="' + row.month + ': ' + val + '"></div>';
    });
    h += '</div>';
    h += '<div style="display:flex;gap:2px;margin-top:2px">';
    history.forEach(function(row) {
      h += '<div style="flex:1;font-size:9px;color:var(--g400);text-align:center">' + row.month.slice(5) + '</div>';
    });
    h += '</div></div>';
  });
  h += '</div>';
  h += '<div style="background:var(--greenPale);border-radius:7px;padding:10px;font-size:12px;color:var(--gd)">';
  h += '🤖 AI分析: 部署频率提升100%、MTTR下降57%，达到<strong>DORA精英水平</strong>。建议下一步优化变更失败率（目标降至2%以下）。';
  h += '</div>';
  el.innerHTML = h;
}

function renderDeployFreqChart() {
  var el = document.getElementById('deploy-heatmap');
  if (!el) return;
  var days = ['周一','周二','周三','周四','周五','周六','周日'];
  var weeks = 4;
  var h = '<div style="font-size:12px;font-weight:700;margin-bottom:8px;color:var(--g600)">部署热力图（近4周）</div>';
  h += '<div style="display:flex;gap:4px">';
  h += '<div style="display:flex;flex-direction:column;gap:4px;padding-top:20px">';
  days.forEach(function(d) {
    h += '<div style="font-size:10px;color:var(--g400);height:16px;line-height:16px">' + d + '</div>';
  });
  h += '</div><div style="flex:1;display:flex;gap:4px">';
  for (var w = 0; w < weeks; w++) {
    h += '<div style="flex:1;display:flex;flex-direction:column;gap:4px">';
    h += '<div style="font-size:10px;color:var(--g400);text-align:center;height:16px">W' + (w+1) + '</div>';
    for (var d = 0; d < 7; d++) {
      var isWeekend = d >= 5;
      var cnt = isWeekend ? Math.floor(Math.random()*3) : Math.floor(Math.random()*8)+1;
      var opacity = cnt === 0 ? 0.05 : (cnt/8 * 0.9 + 0.1);
      h += '<div style="height:16px;border-radius:2px;background:var(--gp);opacity:' + opacity + ';cursor:pointer" title="' + cnt + '次部署" onclick="notify(\'' + cnt + '次部署\',\'info\')"></div>';
    }
    h += '</div>';
  }
  h += '</div></div>';
  el.innerHTML = h;
}

function renderLeadTimeBreakdown() {
  var el = document.getElementById('leadtime-breakdown');
  if (!el) return;
  var stages = [
    {name:'需求→开发开始',hours:4.2,color:'var(--pu)',pct:15},
    {name:'开发周期',hours:14.5,color:'var(--bl)',pct:52},
    {name:'代码评审',hours:3.8,color:'var(--am)',pct:14},
    {name:'CI/CD流水线',hours:1.2,color:'#10b981',pct:4},
    {name:'测试验证',hours:3.8,color:'var(--gp)',pct:14},
    {name:'发布审批',hours:0.3,color:'var(--g300)',pct:1},
  ];
  var h = '<div style="font-size:12px;font-weight:700;margin-bottom:8px;color:var(--g600)">变更前置时间拆解（总: 28.8小时）</div>';
  stages.forEach(function(s) {
    h += '<div style="margin-bottom:7px">';
    h += '<div style="display:flex;justify-content:space-between;font-size:11.5px;margin-bottom:3px">';
    h += '<span>' + s.name + '</span><span style="font-weight:700;color:' + s.color + '">' + s.hours + 'h (' + s.pct + '%)</span></div>';
    h += '<div class="pb" style="height:8px"><div class="pf" style="width:' + s.pct + '%;background:' + s.color + '"></div></div>';
    h += '</div>';
  });
  h += '<div style="font-size:12px;color:var(--g500);margin-top:6px">💡 优化建议：开发周期占比52%，AI代码辅助可降低至35%，预计节省5.4小时</div>';
  el.innerHTML = h;
}

// ── Patch existing renderPage to add new pages ────────────────────
var _origRenderPage = renderPage;
function renderPage(name) {
  if (name === 'pipeline') renderPipeline();
  else if (name === 'release') renderRelease();
  else if (name === 'aispec') renderAISpec();
  else if (name === 'aiagents') renderAIAgents();
  else if (name === 'featureflag') renderFeatureFlag();
  else if (name === 'devops') renderDevOpsAnalytics();
  else _origRenderPage(name);
}
