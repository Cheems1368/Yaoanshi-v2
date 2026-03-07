import type { MedicationCategory, MedicationItem } from '@/modules/patient/types/rationalMedication'

const categories: MedicationCategory[] = [
  {
    id: 'cardiovascular',
    name: '心脑血管用药',
    description: '用于高血压、冠心病、脑卒中二级预防等场景的常见药物知识。',
    tags: ['高血压', '卒中预防', '心血管'],
    topics: [
      {
        id: 'antiplatelet',
        name: '抗血小板药',
        description: '用于降低血栓形成风险，常见于卒中和冠心病长期管理。',
        categoryId: 'cardiovascular',
        categoryName: '心脑血管用药',
        items: [
          {
            id: 'aspirin-enteric',
            name: '阿司匹林肠溶片',
            genericName: '阿司匹林',
            categoryId: 'cardiovascular',
            topicId: 'antiplatelet',
            categoryName: '心脑血管用药',
            topicName: '抗血小板药',
            indications: '缺血性卒中、冠心病等二级预防。',
            dosage: '常见维持剂量为每日一次，遵医嘱使用。',
            precautions: '胃溃疡、活动性出血患者慎用；出现黑便需及时就医。',
            sideEffects: '胃部不适、出血风险增加。',
            interactions: '与抗凝药联合时出血风险升高，需医生评估。',
            storage: '阴凉干燥处保存，避免受潮。'
          },
          {
            id: 'clopidogrel',
            name: '氯吡格雷片',
            genericName: '氯吡格雷',
            categoryId: 'cardiovascular',
            topicId: 'antiplatelet',
            categoryName: '心脑血管用药',
            topicName: '抗血小板药',
            indications: '用于动脉粥样硬化相关血栓事件预防。',
            dosage: '通常每日一次，固定时间服药依从性更好。',
            precautions: '手术前是否停药需由医生决定，不可自行停药。',
            sideEffects: '皮下瘀斑、鼻出血、胃肠不适。',
            interactions: '与部分抑酸药联用可能影响药效，应咨询医生。',
            storage: '密封保存，远离儿童。'
          }
        ]
      },
      {
        id: 'antihypertensive',
        name: '降压药',
        description: '控制血压、降低卒中和心血管事件风险。',
        categoryId: 'cardiovascular',
        categoryName: '心脑血管用药',
        items: [
          {
            id: 'amlodipine',
            name: '氨氯地平片',
            genericName: '苯磺酸氨氯地平',
            categoryId: 'cardiovascular',
            topicId: 'antihypertensive',
            categoryName: '心脑血管用药',
            topicName: '降压药',
            indications: '高血压、稳定型心绞痛。',
            dosage: '多为每日一次，建议固定时段服药。',
            precautions: '血压过低、明显头晕时需联系医生调整方案。',
            sideEffects: '踝部水肿、面部潮红、头痛。',
            interactions: '与其他降压药同用需关注低血压风险。',
            storage: '常温保存，避光。'
          },
          {
            id: 'valsartan',
            name: '缬沙坦胶囊',
            genericName: '缬沙坦',
            categoryId: 'cardiovascular',
            topicId: 'antihypertensive',
            categoryName: '心脑血管用药',
            topicName: '降压药',
            indications: '高血压管理，部分患者用于心衰治疗。',
            dosage: '每日一次或遵医嘱分次服用。',
            precautions: '肾功能异常或高钾风险患者需定期复查。',
            sideEffects: '头晕、乏力、血钾升高。',
            interactions: '与保钾利尿剂联用需监测血钾。',
            storage: '干燥处保存。'
          }
        ]
      }
    ]
  },
  {
    id: 'endocrine',
    name: '糖脂代谢用药',
    description: '面向糖尿病、血脂异常等慢病管理的常见药物。',
    tags: ['糖尿病', '血脂管理', '代谢'],
    topics: [
      {
        id: 'hypoglycemic',
        name: '口服降糖药',
        description: '用于2型糖尿病长期血糖管理。',
        categoryId: 'endocrine',
        categoryName: '糖脂代谢用药',
        items: [
          {
            id: 'metformin',
            name: '二甲双胍片',
            genericName: '二甲双胍',
            categoryId: 'endocrine',
            topicId: 'hypoglycemic',
            categoryName: '糖脂代谢用药',
            topicName: '口服降糖药',
            indications: '2型糖尿病基础治疗常用药。',
            dosage: '常从小剂量起始，餐中或餐后服用。',
            precautions: '胃肠反应明显或肾功能下降需及时就医评估。',
            sideEffects: '腹泻、恶心、食欲下降。',
            interactions: '与其他降糖方案联用时注意低血糖风险。',
            storage: '室温干燥保存。'
          },
          {
            id: 'acarbose',
            name: '阿卡波糖片',
            genericName: '阿卡波糖',
            categoryId: 'endocrine',
            topicId: 'hypoglycemic',
            categoryName: '糖脂代谢用药',
            topicName: '口服降糖药',
            indications: '餐后血糖偏高的2型糖尿病患者。',
            dosage: '通常随第一口主食嚼服或整片吞服。',
            precautions: '漏服后不建议补服，按下一餐继续。',
            sideEffects: '腹胀、排气增多。',
            interactions: '与胰岛素或磺脲类联用可能增加低血糖风险。',
            storage: '避潮保存。'
          }
        ]
      },
      {
        id: 'lipid-lowering',
        name: '调脂药',
        description: '用于降低胆固醇，减少心脑血管事件风险。',
        categoryId: 'endocrine',
        categoryName: '糖脂代谢用药',
        items: [
          {
            id: 'atorvastatin',
            name: '阿托伐他汀钙片',
            genericName: '阿托伐他汀',
            categoryId: 'endocrine',
            topicId: 'lipid-lowering',
            categoryName: '糖脂代谢用药',
            topicName: '调脂药',
            indications: '高胆固醇血症和动脉粥样硬化风险管理。',
            dosage: '常见每日一次，按医生建议固定时间服用。',
            precautions: '肌肉酸痛明显或尿色异常需及时就医。',
            sideEffects: '轻度肝酶升高、肌痛。',
            interactions: '与部分抗菌药、抗真菌药联用需谨慎。',
            storage: '避光、干燥保存。'
          }
        ]
      }
    ]
  },
  {
    id: 'respiratory',
    name: '呼吸系统用药',
    description: '咳嗽、慢阻肺、哮喘等常见呼吸症状相关用药知识。',
    tags: ['咳嗽', '哮喘', '慢阻肺'],
    topics: [
      {
        id: 'expectorant',
        name: '祛痰止咳药',
        description: '缓解咳嗽、痰液黏稠等症状。',
        categoryId: 'respiratory',
        categoryName: '呼吸系统用药',
        items: [
          {
            id: 'ambroxol',
            name: '盐酸氨溴索片',
            genericName: '盐酸氨溴索',
            categoryId: 'respiratory',
            topicId: 'expectorant',
            categoryName: '呼吸系统用药',
            topicName: '祛痰止咳药',
            indications: '痰液黏稠不易咳出的呼吸道症状。',
            dosage: '按说明书或医嘱规律服用。',
            precautions: '症状持续不缓解需排查感染等原因。',
            sideEffects: '胃部不适、口干。',
            interactions: '与镇咳药合用需遵医嘱，避免痰液潴留。',
            storage: '常温密封保存。'
          }
        ]
      },
      {
        id: 'inhalation',
        name: '吸入制剂',
        description: '哮喘或慢阻肺患者常用长期控制方案。',
        categoryId: 'respiratory',
        categoryName: '呼吸系统用药',
        items: [
          {
            id: 'budesonide-formoterol',
            name: '布地奈德福莫特罗吸入粉雾剂',
            genericName: '布地奈德/福莫特罗',
            categoryId: 'respiratory',
            topicId: 'inhalation',
            categoryName: '呼吸系统用药',
            topicName: '吸入制剂',
            indications: '哮喘长期控制、部分慢阻肺维持治疗。',
            dosage: '按处方剂量吸入，使用后漱口。',
            precautions: '吸入装置操作错误会明显影响疗效。',
            sideEffects: '咽部不适、声音嘶哑、心悸。',
            interactions: '与其他支气管扩张剂联合需医生评估。',
            storage: '干燥处保存，防高温。'
          }
        ]
      }
    ]
  },
  {
    id: 'digestive',
    name: '消化系统用药',
    description: '胃酸、胃炎、消化不良等常见消化系统问题相关知识。',
    tags: ['胃酸', '胃炎', '消化不良'],
    topics: [
      {
        id: 'acid-suppression',
        name: '抑酸药',
        description: '用于胃酸相关症状和胃食管反流管理。',
        categoryId: 'digestive',
        categoryName: '消化系统用药',
        items: [
          {
            id: 'omeprazole',
            name: '奥美拉唑肠溶胶囊',
            genericName: '奥美拉唑',
            categoryId: 'digestive',
            topicId: 'acid-suppression',
            categoryName: '消化系统用药',
            topicName: '抑酸药',
            indications: '胃炎、消化性溃疡、反流相关症状。',
            dosage: '通常餐前服用，按疗程使用。',
            precautions: '长期使用需医生评估，避免自行长期续用。',
            sideEffects: '腹胀、头痛、恶心。',
            interactions: '与部分抗血小板药联用时需评估方案。',
            storage: '避光、密封保存。'
          }
        ]
      }
    ]
  },
  {
    id: 'antiinfective',
    name: '抗感染用药',
    description: '抗菌药使用原则和常见注意事项。',
    tags: ['抗生素', '感染', '规范用药'],
    topics: [
      {
        id: 'beta-lactam',
        name: '青霉素类/头孢类',
        description: '社区常见感染中常见的抗菌药类别。',
        categoryId: 'antiinfective',
        categoryName: '抗感染用药',
        items: [
          {
            id: 'amoxicillin',
            name: '阿莫西林胶囊',
            genericName: '阿莫西林',
            categoryId: 'antiinfective',
            topicId: 'beta-lactam',
            categoryName: '抗感染用药',
            topicName: '青霉素类/头孢类',
            indications: '敏感菌感染，如呼吸道、泌尿道部分感染。',
            dosage: '按医嘱和疗程服用，不可擅自停药。',
            precautions: '青霉素过敏史患者禁用或慎用。',
            sideEffects: '皮疹、胃肠道反应。',
            interactions: '与部分抗凝药联用需监测出血风险。',
            storage: '阴凉干燥处保存。'
          }
        ]
      }
    ]
  }
]

const itemsById = categories
  .flatMap((category) => category.topics.flatMap((topic) => topic.items))
  .reduce<Record<string, MedicationItem>>((acc, item) => {
    acc[item.id] = item
    return acc
  }, {})

export const rationalMedicationData = {
  categories,
  getCategoryById(categoryId: string) {
    return categories.find((category) => category.id === categoryId)
  },
  getItemById(itemId: string) {
    return itemsById[itemId]
  },
  searchCategories(keyword: string) {
    const query = keyword.trim().toLowerCase()
    if (!query) {
      return categories
    }

    return categories.filter((category) => {
      const inCategory =
        category.name.toLowerCase().includes(query) ||
        category.description.toLowerCase().includes(query) ||
        category.tags.some((tag) => tag.toLowerCase().includes(query))
      if (inCategory) {
        return true
      }

      return category.topics.some((topic) => {
        if (
          topic.name.toLowerCase().includes(query) ||
          topic.description.toLowerCase().includes(query)
        ) {
          return true
        }

        return topic.items.some((item) => {
          return (
            item.name.toLowerCase().includes(query) ||
            item.genericName.toLowerCase().includes(query) ||
            item.indications.toLowerCase().includes(query)
          )
        })
      })
    })
  }
}
