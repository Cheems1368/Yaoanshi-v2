export interface MedicationItem {
  id: string
  name: string
  genericName: string
  categoryId: string
  topicId: string
  categoryName: string
  topicName: string
  indications: string
  dosage: string
  precautions: string
  sideEffects: string
  interactions: string
  storage: string
}

export interface MedicationTopic {
  id: string
  name: string
  description: string
  categoryId: string
  categoryName: string
  items: MedicationItem[]
}

export interface MedicationCategory {
  id: string
  name: string
  description: string
  tags: string[]
  topics: MedicationTopic[]
}
