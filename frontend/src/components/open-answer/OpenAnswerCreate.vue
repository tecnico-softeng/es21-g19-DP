<template>
  <v-row>
    <v-textarea
      v-model="sQuestionDetails.correctAnswer"
      label="Correct Answer"
      :rules="[(v) => !!v || 'Correct answer is required']"
      auto-grow
      required
      data-cy="correctAnswerTextArea"
      rows="4"
    ></v-textarea>
  </v-row>
</template>

<script lang="ts">
import { Component, Vue, PropSync } from 'vue-property-decorator';
import OpenAnswerQuestionDetails from '@/models/management/questions/OpenAnswerQuestionDetails';

@Component
export default class OpenAnswerCreate extends Vue {
  @PropSync('questionDetails', { type: OpenAnswerQuestionDetails })
  sQuestionDetails!: OpenAnswerQuestionDetails;
}
</script>
