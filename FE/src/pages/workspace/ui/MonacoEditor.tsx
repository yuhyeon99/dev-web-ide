import MonacoEditor from '@monaco-editor/react';

type MonacoEditorComponentProps = {
  language: string;
  value: string;
};

const MonacoEditorComponent = ({
  language,
  value,
}: MonacoEditorComponentProps) => {
  return (
    <MonacoEditor
      height="100%"
      language={language}
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
