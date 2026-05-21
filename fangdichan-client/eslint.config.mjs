import pluginVue from 'eslint-plugin-vue'
import vuePrettier from '@vue/eslint-config-prettier'

export default [
  ...pluginVue.configs['flat/recommended'],
  vuePrettier,
  {
    rules: {
      'vue/multi-word-component-names': 'off',
      'vue/no-unused-vars': ['error', { ignorePattern: '^_' }],
      'no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      'prefer-const': 'error',
      'no-var': 'error'
    },
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: {
        electron: 'readonly'
      }
    }
  }
]
