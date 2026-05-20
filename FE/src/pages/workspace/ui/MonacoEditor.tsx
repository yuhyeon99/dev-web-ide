import MonacoEditor from '@monaco-editor/react';

const MonacoEditorComponent = (props: any) => {
  return (
    <MonacoEditor
      height="100%"
      theme="vs-dark"
      defaultLanguage="javascript"
      defaultValue="// some comment"
      {...props}
    />
  );
};

export default MonacoEditorComponent;
