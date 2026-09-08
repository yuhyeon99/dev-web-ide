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
  const applyingRemoteContentRef = useRef(false);
  const connectionRef = useRef<CrdtClientConnection | null>(null);

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
        if (
          origin === 'remote' ||
          origin === 'remote-content' ||
          origin === 'initial-load'
        ) {
          return;
        }

        connection?.sendUpdate(update);
      };

      ydoc.on('update', sendLocalUpdate);
      connection = createCrdtClient({
        clientId: collaboration.clientId,
        fileId: collaboration.fileId,
        onRemoteContent: (content) => {
          const currentContent = ytext.toString();

          if (currentContent === content) {
            return;
          }

          applyingRemoteContentRef.current = true;
          ydoc.transact(() => {
            ytext.delete(0, ytext.length);
            ytext.insert(0, content);
          }, 'remote-content');
          onChange(content);
          queueMicrotask(() => {
            applyingRemoteContentRef.current = false;
          });
        },
        projectId: collaboration.projectId,
        onRemoteUpdate: (update) => {
          Y.applyUpdate(ydoc, update, 'remote');
        },
      });
      connectionRef.current = connection;

      cleanupRef.current = () => {
        ydoc.off('update', sendLocalUpdate);
        connection?.disconnect();
        connectionRef.current = null;
        binding.destroy();
        ydoc.destroy();
      };
    },
    [collaboration, onChange],
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
      onChange={(nextValue) => {
        const content = nextValue ?? '';

        onChange(content);

        if (!applyingRemoteContentRef.current) {
          connectionRef.current?.sendContent(content);
        }
      }}
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
