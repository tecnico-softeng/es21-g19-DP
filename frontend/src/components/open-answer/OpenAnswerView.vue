<template>
  <div>
    Correct Answer:
    <span v-html="convertMarkDown(questionDetails.correctAnswer)" />
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import OpenAnswerQuestionDetails from '@/models/management/questions/OpenAnswerQuestionDetails';

@Component
export default class ShowOpenAnswerQuestion extends Vue {
  @Prop({ type: OpenAnswerQuestionDetails, required: true })
  readonly questionDetails!: OpenAnswerQuestionDetails;

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>
