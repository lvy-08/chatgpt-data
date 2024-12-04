package cn.bug.chatgpt.data.trigger.http;

import cn.bug.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bug.chatgpt.data.domain.openai.model.entity.MessageEntity;
import cn.bug.chatgpt.data.domain.openai.service.IChatService;
import cn.bug.chatgpt.data.trigger.http.dto.ChatGPTRequestDTO;
import cn.bug.chatgpt.data.types.exception.ChatGPTException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2023-07-16 09:19
 */
@Slf4j
@RestController()//自动将方法返回值序列化为 JSON 响应
@CrossOrigin("${app.config.cross-origin}")//允许前端从不同的源（域名、端口）访问该接口
@RequestMapping("/api/${app.config.api-version}/")//定义请求的基础路径
public class ChatGPTAIServiceController {

    @Resource//按名称注入 IChatService 接口的实现类实例
    private IChatService chatService;

    /**
     * 流式问题，ChatGPT 请求接口
     * <p>
     * curl -X POST \
     * http://localhost:8090/api/v1/chat/completions \
     * -H 'Content-Type: application/json;charset=utf-8' \
     * -H 'Authorization: b8b6' \
     * -d '{
     * "messages": [
     * {
     * "content": "写一个java冒泡排序",
     * "role": "user"
     * }
     * ],
     * "model": "gpt-3.5-turbo"
     * }'
     */
    @RequestMapping(value = "chat/completions", method = RequestMethod.POST)//具体路径：/api/v1/chat/completions，POST 方法
    public ResponseBodyEmitter completionsStream(@RequestBody ChatGPTRequestDTO request, @RequestHeader("Authorization") String token, HttpServletResponse response) {//接收客户端的 JSON 请求体，并将其反序列化为 ChatGPTRequestDTO 对象。//从请求头中提取 Authorization 信息（如身份认证 Token//提供对 HTTP 响应的直接操作，如设置内容类型、编码等//返回ResponseBodyEmitter 对象，实现流式输出，将数据逐步推送到客户端
        log.info("流式问答请求开始，使用模型：{} 请求信息：{}", request.getModel(), JSON.toJSONString(request.getMessages()));//序列化为 JSON
        try {//设置响应头，表明这是一个 SSE（Server-Sent Events）流式响应：
            // 1. 基础配置；流式输出、编码、禁用缓存
            response.setContentType("text/event-stream");//指定响应类型为事件流
            response.setCharacterEncoding("UTF-8");//指定编码为 UTF-8
            //response.setHeader("Cache-Control", "no-cache");//禁用浏览器缓存，确保数据实时推送

            // 2. 构建参数
            ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                    .token(token)
                    .model(request.getModel())
                    .messages(request.getMessages().stream()
                            .map(entity -> MessageEntity.builder()// 使用 stream() 和 map() 方法，将 ChatGPTRequestDTO 中的消息对象转换为 MessageEntity 对象
                                    .role(entity.getRole())
                                    .content(entity.getContent())
                                    //.name(entity.getName())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

            // 3. 请求结果&返回
            return chatService.completions(chatProcessAggregate,response);//返回流式响应对象 ResponseBodyEmitter
        } catch (Exception e) {
            log.error("流式应答，请求模型：{} 发生异常", request.getModel(), e);
            throw new ChatGPTException(e.getMessage());
        }
    }

}
