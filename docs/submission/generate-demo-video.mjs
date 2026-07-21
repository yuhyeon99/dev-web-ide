import { createRequire } from 'node:module';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const require = createRequire(import.meta.url);
const { chromium } = require('/tmp/dev-web-ide-submission-tools/node_modules/playwright-core');

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const sourceUrl = pathToFileURL(path.join(__dirname, 'demo-video-source.html')).href;
const outputPath = path.join(__dirname, 'video', 'dev-web-ide-demo.webm');

const browser = await chromium.launch({
  headless: true,
  executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
});

try {
  const context = await browser.newContext({
    acceptDownloads: true,
    viewport: {
      width: 1280,
      height: 720,
    },
  });
  const page = await context.newPage();

  await page.goto(sourceUrl);
  const downloadPromise = page.waitForEvent('download', {
    timeout: 70000,
  });
  await page.getByRole('button', { name: 'Generate WebM' }).click();
  const download = await downloadPromise;

  await download.saveAs(outputPath);
  console.log(outputPath);
} finally {
  await browser.close();
}
