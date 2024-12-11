package cn.bug.chatgpt.data.domain.openai.service.rule.impl;

import cn.bug.chatgpt.data.domain.openai.annotation.LogicStrategy;
import cn.bug.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bug.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.bug.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bug.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.bug.chatgpt.data.domain.openai.model.valobj.UserAccountStatusVO;
import cn.bug.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import cn.bug.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 账户校验
 * @create 2023-10-03 17:44
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.ACCOUNT_STATUS)
public class AccountStatusFilter implements ILogicFilter<UserAccountQuotaEntity> {

    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) throws Exception {
        // 账户可用，直接放行
        if (UserAccountStatusVO.AVAILABLE.equals(data.getUserAccountStatusVO())) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .info("您的账户已冻结，暂时不可使用。如果有疑问，可以联系客户解冻账户。")
                .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();
    }

}
