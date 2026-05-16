package cn.com.bosssfot.dv.plm.inception.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.inception.domain.Inception;

public interface InceptionMapper {
    List<Inception> selectInceptionList(Inception inception);
    Inception selectInceptionById(Long inceptionId);
    int insertInception(Inception inception);
    int updateInception(Inception inception);
    int deleteInceptionByIds(Long[] inceptionIds);

    /** ADR: 查"以 prefix 开头的 inception_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
