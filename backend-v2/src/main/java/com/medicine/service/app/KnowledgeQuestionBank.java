package com.medicine.service.app;

import com.medicine.api.dto.assessment.KnowledgeQuestionDTO;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Component
public class KnowledgeQuestionBank {
    public static final String VERSION = "medication-literacy-v1";
    private final Map<String, Question> questions = createQuestions();

    public List<KnowledgeQuestionDTO> publicQuestions() {
        return questions.values().stream().map(question -> new KnowledgeQuestionDTO(
                question.code(), question.text(), question.options().stream()
                .map(option -> new KnowledgeQuestionDTO.OptionDTO(option.code(), option.text())).toList()
        )).toList();
    }

    public Question find(String code) { return questions.get(code); }
    public int size() { return questions.size(); }

    private Map<String, Question> createQuestions() {
        Map<String, Question> result = new LinkedHashMap<>();
        add(result, "Q01", "已经与医生或药师确认的用药计划，日常应如何执行？", "A",
                "按确认的药名、剂量和时间执行", "感觉好转就自行减少剂量", "参考他人的方案调整");
        add(result, "Q02", "发现漏服后，首先应怎么做？", "B",
                "下一次自行服用双倍剂量", "查看已确认的漏服处理说明或咨询专业人员", "永久停用该药");
        add(result, "Q03", "出现呼吸困难、意识异常等严重情况时，应优先怎么做？", "A",
                "优先联系急救或立即线下就医", "继续在线等待回复", "自行尝试多种药物");
        add(result, "Q04", "院外购买或其他医生新开的药，应如何纳入当前用药管理？", "C",
                "无需告知任何人", "替换掉原计划中名称相近的药", "如实登记并等待医生或药师复核");
        add(result, "Q05", "处方图片的 OCR 识别结果应如何看待？", "B",
                "识别结果一定准确", "只能辅助录入，仍需核对或专业复核", "可直接据此自行开药");
        add(result, "Q06", "系统中的 AI 用药助手可以替代医生诊疗吗？", "C",
                "可以独立诊断并调整处方", "可以替代急救", "不能，只能提供一般信息和就医提示");
        add(result, "Q07", "就诊或咨询时，怎样提供用药信息更安全？", "A",
                "携带或展示完整的当前用药清单", "只说记得最清楚的一种药", "隐瞒院外用药");
        add(result, "Q08", "对处方药的停药或改量，正确做法是？", "B",
                "根据当天感觉自行决定", "先由医生或药师确认", "听非专业人员建议直接修改");
        add(result, "Q09", "服药打卡时应记录什么？", "C",
                "为提高完成率一律选按时", "让系统随机选择", "按实际发生情况记录");
        add(result, "Q10", "出现疑似不良反应时，较合适的做法是？", "B",
                "删除记录避免担心", "记录症状和时间，并按严重程度及时求助", "自行增加其他药物抵消");
        return Collections.unmodifiableMap(result);
    }

    private void add(Map<String, Question> target, String code, String text, String correctOption,
                     String optionA, String optionB, String optionC) {
        target.put(code, new Question(code, text, List.of(
                new Option("A", optionA), new Option("B", optionB), new Option("C", optionC)
        ), correctOption));
    }

    public record Question(String code, String text, List<Option> options, String correctOption) {
        public boolean hasOption(String code) { return options.stream().anyMatch(option -> option.code().equals(code)); }
    }
    public record Option(String code, String text) { }
}
