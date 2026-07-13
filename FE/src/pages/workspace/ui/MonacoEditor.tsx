/* eslint-disable no-unused-vars */

import { useCallback, useEffect, useRef } from 'react';
import MonacoEditor, { type OnMount } from '@monaco-editor/react';
import { MonacoBinding } from 'y-monaco';
import * as Y from 'yjs';

import {
  createCrdtClient,
  type CrdtClientConnection,
} from '@/shared/realtime/workspace-collaboration';

type EditorCollaboration = {
  clientId: string;
  fileId: number;
  initialContent: string;
  projectId: number;
};

type MonacoEditorComponentProps = {
  collaboration?: EditorCollaboration;
  language: string;
  onChange: (value: string) => void;
  value: string;
};

const MonacoEditorComponent = ({
  collaboration,
  language,
  onChange,
  value,
}: MonacoEditorComponentProps) => {
  const cleanupRef = useRef<(() => void) | null>(null);

  const handleMount = useCallback<OnMount>(
    (editor) => {
      if (!collaboration) {
        return;
      }

      const model = editor.getModel();

      if (!model) {
        return;
      }

      const ydoc = new Y.Doc();
      const ytext = ydoc.getText('monaco');

      if (collaboration.initialContent) {
        ydoc.transact(() => {
          ytext.insert(0, collaboration.initialContent);
        }, 'initial-load');
      }

      const binding = new MonacoBinding(ytext, model, new Set([editor]));
      let connection: CrdtClientConnection | null = null;
      const sendLocalUpdate = (update: Uint8Array, origin: unknown) => {
        if (origin === 'remote' || origin === 'initial-load') {
          return;
        }

        connection?.sendUpdate(update);
      };

      ydoc.on('update', sendLocalUpdate);
      connection = createCrdtClient({
        clientId: collaboration.clientId,
        fileId: collaboration.fileId,
        projectId: collaboration.projectId,
        onRemoteUpdate: (update) => {
          Y.applyUpdate(ydoc, update, 'remote');
        },
      });

      cleanupRef.current = () => {
        ydoc.off('update', sendLocalUpdate);
        connection?.disconnect();
        binding.destroy();
        ydoc.destroy();
      };
    },
    [collaboration],
  );

  useEffect(() => {
    return () => {
      cleanupRef.current?.();
      cleanupRef.current = null;
    };
  }, []);

  return (
    <MonacoEditor
      defaultValue={collaboration ? value : undefined}
      height="100%"
      language={language}
      onMount={handleMount}
      onChange={(nextValue) => onChange(nextValue ?? '')}
      theme="vs-dark"
      value={collaboration ? undefined : value}
      options={{
        minimap: { enabled: false },
        scrollBeyondLastLine: false,
        wordWrap: 'on',
      }}
    />
  );
};

export default MonacoEditorComponent;
