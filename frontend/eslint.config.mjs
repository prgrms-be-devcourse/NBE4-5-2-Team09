import globals from 'globals';
import pluginJs from '@eslint/js';
import tseslint from 'typescript-eslint';
import pluginReact from 'eslint-plugin-react';
import eslintPluginUnusedImports from 'eslint-plugin-unused-imports';

/** @type {import('eslint').Linter.Config[]} */
export default [
  { files: ['**/*.{js,mjs,cjs,ts,jsx,tsx}'] },
  { languageOptions: { globals: globals.browser } },
  pluginJs.configs.recommended,
  ...tseslint.configs.recommended,
  pluginReact.configs.flat.recommended,
  {
    settings: {
      react: {
        version: 'detect',
      },
    },
  },
  {
    ignores: [
      'node_modules',
      '.next',
      '.turbo',
      '.vercel',
      'public',
      '**/generated/**',
      '**/*.min.js',
      '**/*.test.js',
      '*.json',
    ],
  },
  {
    rules: {
      'react/react-in-jsx-scope': 'off',
    },
  },
  {
    plugins: {
      'unused-imports': eslintPluginUnusedImports,
    },
    rules: {
      'unused-imports/no-unused-imports': 'error',
      'unused-imports/no-unused-vars': [
        'warn',
        { vars: 'all', args: 'after-used', argsIgnorePattern: '^_' },
      ],
    },
  },
];
