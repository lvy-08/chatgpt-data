package cn.bug.chatgpt.data.domain.openai.service;

import cn.bug.chatgpt.common.Constants;
import cn.bug.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bug.chatgpt.domain.chat.ChatChoice;
import cn.bug.chatgpt.domain.chat.ChatCompletionRequest;
import cn.bug.chatgpt.domain.chat.ChatCompletionResponse;
import cn.bug.chatgpt.domain.chat.Message;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2023-07-22 21:11
 */
@Service
public class ChatService extends AbstractChatService {

    @Override
    protected void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter, HttpServletResponse response) throws JsonProcessingException {
        // 1. 请求消息
        List<Message> messages = chatProcess.getMessages().stream()//将转换后的 List<Message> 赋值给 messages 变量    //将 List 转换为 Stream，便于使用流式操作对每个元素进行处理。
                .map(entity -> Message.builder()//map 将每个 entity 对象转换为一个新的 Message 对象
                        .role(Constants.Role.valueOf(entity.getRole().toUpperCase()))
                        .content(entity.getContent())
                        //.name(entity.getName())
                        .build())
                .collect(Collectors.toList());//将流中的转换结果收集到一个新的 List<Message> 中。

        // 2. 封装参数
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .stream(true)
                .messages(messages)
                .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                .build();

        // 3.2 请求应答
        openAiSession.chatCompletions(chatCompletion, new EventSourceListener() {//匿名实现，用于监听来自 OpenAI 的 SSE 流
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {//当有新消息流数据时触发
                ChatCompletionResponse chatCompletionResponse = JSON.parseObject(data, ChatCompletionResponse.class);
                List<ChatChoice> choices = chatCompletionResponse.getChoices();
                for (ChatChoice chatChoice : choices) {
                    Message delta = chatChoice.getDelta();
                    if (Constants.Role.ASSISTANT.getCode().equals(delta.getRole())) continue;

                    // 应答完成
                    String finishReason = chatChoice.getFinishReason();
                    if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
                        emitter.complete();//结束服务器端事件流
                        break;
                    }

                    // 发送信息
                    try {
                        emitter.send(delta.getContent());//推送消息到客户端。
                        response.flushBuffer(); // 强制刷新缓冲.把到来的数据立即发出去
                        Thread.sleep(10); // 模拟逐步生成和发送
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });
    }

}
