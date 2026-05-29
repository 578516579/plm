package cn.com.bosssfot.dv.plm.integration.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cn.com.bosssfot.dv.plm.integration.domain.IntegrationUserMapping;
import cn.com.bosssfot.dv.plm.integration.mapper.IntegrationUserMappingMapper;

/**
 * {@link IntegrationUserMappingServiceImpl} 单元测试 — task #7 jacoco 0% → ≥60%。
 *
 * <p>覆盖:3 委托(select/insert/update/delete)+ resolveUserIdByExternalAccount
 * 双向解析(正反 + null 兜底 + touchLastUsed 异常吞掉路径)。
 */
@ExtendWith(MockitoExtension.class)
class IntegrationUserMappingServiceImplTest {

    @Mock
    private IntegrationUserMappingMapper mapper;

    @InjectMocks
    private IntegrationUserMappingServiceImpl service;

    @Nested
    @DisplayName("简单委托")
    class DelegationTests {
        @Test
        @DisplayName("selectList 委托 mapper")
        void selectList() {
            IntegrationUserMapping q = new IntegrationUserMapping();
            when(mapper.selectList(q)).thenReturn(List.of(new IntegrationUserMapping()));
            assertThat(service.selectList(q)).hasSize(1);
        }

        @Test
        @DisplayName("selectById 委托 mapper")
        void selectById() {
            IntegrationUserMapping row = new IntegrationUserMapping();
            when(mapper.selectById(1L)).thenReturn(row);
            assertThat(service.selectById(1L)).isSameAs(row);
        }

        @Test
        @DisplayName("insert 委托 mapper")
        void insert() {
            IntegrationUserMapping m = new IntegrationUserMapping();
            when(mapper.insert(m)).thenReturn(1);
            assertThat(service.insert(m)).isEqualTo(1);
        }

        @Test
        @DisplayName("update 委托 mapper")
        void update() {
            IntegrationUserMapping m = new IntegrationUserMapping();
            when(mapper.update(m)).thenReturn(1);
            assertThat(service.update(m)).isEqualTo(1);
        }

        @Test
        @DisplayName("deleteByIds 委托 mapper")
        void delete() {
            Long[] ids = { 1L, 2L };
            when(mapper.deleteByIds(ids)).thenReturn(2);
            assertThat(service.deleteByIds(ids)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("resolveUserIdByExternalAccount 正向解析")
    class ResolveUserIdTests {
        @Test
        @DisplayName("connectorId 为 null → 返 null,不调 mapper")
        void connectorIdNull() {
            assertThat(service.resolveUserIdByExternalAccount(null, "x")).isNull();
            verify(mapper, never()).selectByConnectorAndAccount(eq(0L), eq("x"));
        }

        @Test
        @DisplayName("externalAccount 为空 → 返 null")
        void externalAccountEmpty() {
            assertThat(service.resolveUserIdByExternalAccount(1L, "")).isNull();
        }

        @Test
        @DisplayName("映射不存在 → 返 null + log 容忍")
        void mappingNotFound() {
            when(mapper.selectByConnectorAndAccount(1L, "alice")).thenReturn(null);
            assertThat(service.resolveUserIdByExternalAccount(1L, "alice")).isNull();
        }

        @Test
        @DisplayName("映射存在 → 返 userId + 触发 touchLastUsed")
        void mappingFound() {
            IntegrationUserMapping m = new IntegrationUserMapping();
            m.setId(99L);
            m.setUserId(7L);
            when(mapper.selectByConnectorAndAccount(1L, "alice")).thenReturn(m);

            Long result = service.resolveUserIdByExternalAccount(1L, "alice");

            assertThat(result).isEqualTo(7L);
            verify(mapper).touchLastUsed(99L);
        }

        @Test
        @DisplayName("touchLastUsed 抛异常被吞掉,主路径仍返 userId")
        void touchLastUsedSwallowsException() {
            IntegrationUserMapping m = new IntegrationUserMapping();
            m.setId(99L);
            m.setUserId(7L);
            when(mapper.selectByConnectorAndAccount(1L, "alice")).thenReturn(m);
            org.mockito.Mockito.doThrow(new RuntimeException("DB down"))
                .when(mapper).touchLastUsed(99L);

            assertThat(service.resolveUserIdByExternalAccount(1L, "alice")).isEqualTo(7L);
        }
    }

    @Nested
    @DisplayName("resolveExternalAccountByUserId 反向解析")
    class ResolveExternalAccountTests {
        @Test
        @DisplayName("connectorId 为 null → null")
        void connectorIdNull() {
            assertThat(service.resolveExternalAccountByUserId(null, 1L)).isNull();
        }

        @Test
        @DisplayName("userId 为 null → null")
        void userIdNull() {
            assertThat(service.resolveExternalAccountByUserId(1L, null)).isNull();
        }

        @Test
        @DisplayName("映射不存在 → 返 null")
        void notFound() {
            when(mapper.selectByConnectorAndUserId(1L, 7L)).thenReturn(null);
            assertThat(service.resolveExternalAccountByUserId(1L, 7L)).isNull();
        }

        @Test
        @DisplayName("映射存在 → 返 externalAccount + 触发 touchLastUsed")
        void found() {
            IntegrationUserMapping m = new IntegrationUserMapping();
            m.setId(99L);
            m.setExternalAccount("alice@foo");
            when(mapper.selectByConnectorAndUserId(1L, 7L)).thenReturn(m);

            assertThat(service.resolveExternalAccountByUserId(1L, 7L)).isEqualTo("alice@foo");
            verify(mapper).touchLastUsed(99L);
        }

        @Test
        @DisplayName("touchLastUsed 异常被吞掉,主路径仍返 externalAccount")
        void touchSwallowsException() {
            IntegrationUserMapping m = new IntegrationUserMapping();
            m.setId(99L);
            m.setExternalAccount("alice@foo");
            when(mapper.selectByConnectorAndUserId(1L, 7L)).thenReturn(m);
            org.mockito.Mockito.doThrow(new RuntimeException("DB down"))
                .when(mapper).touchLastUsed(99L);

            assertThat(service.resolveExternalAccountByUserId(1L, 7L)).isEqualTo("alice@foo");
        }
    }
}
