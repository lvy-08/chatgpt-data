package cn.bug.chatgpt.data.domain.openai.service;

import cn.bug.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bug.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.bug.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.bug.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import cn.bug.chatgpt.data.types.common.Constants;
import cn.bug.chatgpt.data.types.exception.ChatGPTException;
import cn.bug.chatgpt.session.OpenAiSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2023-07-22 21:12
 */
@Slf4j
public abstract class AbstractChatService implements IChatService {

    @Resource
    protected OpenAiSession openAiSession;

    @Override
    public ResponseBodyEmitter completions(ChatProcessAggregate chatProcess, HttpServletResponse response) {

        // 2. 请求应答
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
        emitter.onCompletion(() -> {
            log.info("流式问答请求完成，使用模型：{}", chatProcess.getModel());
        });

        emitter.onError(throwable -> log.error("流式问答请求疫情，使用模型：{}", chatProcess.getModel(), throwable));

        // 3. 应答处理
        try {
            // 2. 规则过滤
            RuleLogicEntity<ChatProcessAggregate> ruleLogicEntity = this.doCheckLogic(chatProcess,
                    DefaultLogicFactory.LogicModel.ACCESS_LIMIT.getCode(),
                    DefaultLogicFactory.LogicModel.SENSITIVE_WORD.getCode());

            if (!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType())) {
                emitter.send(ruleLogicEntity.getInfo());
                emitter.complete();
                return emitter;
            }


            this.doMessageResponse(chatProcess, emitter,response);
        } catch (Exception e) {
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }

        // 4. 返回结果
        return emitter;
    }

    protected abstract RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, String... logics) throws Exception;
    protected abstract void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter,HttpServletResponse response) throws JsonProcessingException;


}
