import globals from 'globals';
import pluginJs from '@eslint/js';
import tseslint from 'typescript-eslint';
import pluginReact from 'eslint-plugin-react';
import eslintPluginUnusedImports from 'eslint-plugin-unused-imports';
import eslintPluginImport from 'eslint-plugin-import';

/** @type {import('eslint').Linter.Config[]} */
export default [
  {
    files: ['**/*.{js,mjs,cjs,ts,jsx,tsx}'],
    languageOptions: {
      globals: globals.browser,
      parserOptions: {
        project: './tsconfig.json',
      },
    },
    settings: {
      react: {
        version: 'detect',
      },
      'import/resolver': {
        typescript: {
          project: './tsconfig.json',
        },
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
      'tailwind.config.js',
    ],
  },
  pluginJs.configs.recommended,
  ...tseslint.configs.recommended,
  pluginReact.configs.flat.recommended,
  {
    plugins: {
      import: eslintPluginImport,
      'unused-imports': eslintPluginUnusedImports,
    },
    rules: {
      'react/react-in-jsx-scope': 'off',
      'import/no-unresolved': 'error',
      'unused-imports/no-unused-imports': 'error',
      'unused-imports/no-unused-vars': [
        'warn',
        { vars: 'all', args: 'after-used', argsIgnorePattern: '^_' },
      ],
    },
  },
];
