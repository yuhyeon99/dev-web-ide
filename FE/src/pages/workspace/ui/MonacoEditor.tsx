/* eslint-disable no-unused-vars */

import MonacoEditor from '@monaco-editor/react';

type MonacoEditorComponentProps = {
  language: string;
  onChange: (value: string) => void;
  value: string;
};

const MonacoEditorComponent = ({
  language,
  onChange,
  value,
}: MonacoEditorComponentProps) => {
  return (
    <MonacoEditor
      height="100%"
      language={language}
      onChange={(nextValue) => onChange(nextValue ?? '')}
      theme="vs-dark"
      value={value}
      options={{
        minimap: { enabled: false },
        scrollBeyondLastLine: false,
        wordWrap: 'on',
      }}
    />
  );
};

export default MonacoEditorComponent;
