package cn.bug.chatgpt.data.config;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.utils.InnerWordCharUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 敏感词配置
 * @create 2023-09-16 17:38
 */
@Slf4j
@Configuration
public class SensitiveWordConfig {

    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        return SensitiveWordBs.newInstance()
                .wordReplace((stringBuilder, chars, wordResult, iWordContext) -> {//结果构建器，用于生成替换后的文本。//原始字符数组。//敏感词检测的结果，包含敏感词的位置信息。//上下文，用于传递额外的配置信息。
                    String sensitiveWord = InnerWordCharUtils.getString(chars, wordResult);//InnerWordCharUtils.getString 是一个工具方法，根据 wordResult 中的敏感词起始和结束索引，从 chars 中提取对应的字符。//文本的字符数组（char[]），通常是从输入的字符串分解而来的。//敏感词检测工具返回的一个结果对象。对象可能包含敏感词在字符数组中的起始索引和结束索引
                    log.info("检测到敏感词: {}", sensitiveWord);
                    /* 替换操作，你可以指定的替换为*或者其他
                    else {
                        int wordLength = wordResult.endIndex() - wordResult.startIndex();
                        for (int i = 0; i < wordLength; i++) {
                            stringBuilder.append("");
                        }
                    }*/
                })
                //忽略规则配置
                .ignoreCase(true)//忽略大小写。
                .ignoreWidth(true)//忽略全角和半角的差异。
                .ignoreNumStyle(true)//忽略数字的不同风格。
                .ignoreChineseStyle(true)//忽略中文繁简体差异。
                .ignoreEnglishStyle(true)//忽略英文的书写风格。
                .ignoreRepeat(false)//是否忽略重复字符。
                //敏感词工具支持多种内容的检查：
                .enableNumCheck(true)//检查数字中的敏感信息。
                .enableEmailCheck(true)//检查电子邮件地址中的敏感信息。
                .enableUrlCheck(true)//检查 URL 地址中的敏感信息。
                .enableWordCheck(true)//检查普通文本中的敏感信息。
                //其他配置
                .numCheckLen(1024)//设置数字检查的最大长度，防止超长数字影响性能。
                .init();//初始化敏感词工具，加载默认词库或用户自定义词库。
    }

}
