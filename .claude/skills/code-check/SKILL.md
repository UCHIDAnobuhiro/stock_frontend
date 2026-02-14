---
name: code-check
description: コミット前にコードの品質をレビューする。コードレビュー、品質チェック、差分確認を依頼された際に使用。
---

コミット前にコードの品質をレビューしてください。

## 手順

### Step 1: 差分の取得

1. `git diff` と `git diff --cached` で未コミットの変更を確認
2. 変更がない場合はその旨を伝えて終了
3. `git diff --stat` で変更の概要を把握する

**差分が大きい場合（10ファイル以上または500行以上）**: コンテキストの圧迫を避けるため、`--stat` で全体像を把握し、ファイル単位で `git diff -- <path>` を使って重要な変更から順に確認する。

### Step 2: 秘密情報・不要コードの検出

差分に以下が含まれていないか検査する。1つでも検出した場合は**レビューを中断**し、該当箇所をユーザーに報告する：

- APIキー・トークンのパターン（`sk-`, `ghp_`, `AKIA`, 32文字以上の英数字文字列など）
- ハードコードされた秘密情報（パスワード、接続文字列など）
- conflict marker の残り（<<<<<<< / ======= / >>>>>>>）
- `TODO: remove` や一時的なデバッグコード（`println` によるデバッグ出力、`Log.d` の残り等）

### Step 3: アーキテクチャ・責務の分離

CLAUDE.md のアーキテクチャルールに基づき、以下を検査する：

- **依存関係ルール違反**: ViewModel が DataSource（API）を直接参照していないか（Repository を介すべき）
- **レイヤー責務の混在**: ビジネスロジックが Composable に書かれていないか、UI固有の処理（`R.string` 等）が Repository に入っていないか
- **DTO の漏洩**: data/remote/ の DTO（Dto/Request/Response 接尾辞のクラス）が ViewModel や UI 層に直接公開されていないか（UiState に変換すべき）
- **UiState パターンの遵守**: ViewModel が StateFlow で UiState ドメインモデルを公開し、DTO を直接公開していないか
- **DI の適切さ**: ViewModel が `@HiltViewModel` + `@Inject constructor` を使用しているか、手動でインスタンスを生成していないか
- **パッケージ配置**: 新規ファイルがフィーチャーベースのパッケージ構造に従っているか
  - API/DTO → feature/{機能名}/data/remote/
  - Repository → feature/{機能名}/data/repository/
  - ドメインモデル → feature/{機能名}/domain/model/
  - UI/UiState → feature/{機能名}/ui/
  - ViewModel → feature/{機能名}/viewmodel/
  - 共有コンポーネント → core/

### Step 4: 命名規則

プロジェクト固有の命名規則を検査する（標準的な Kotlin 規約は省略）：

- **UiState クラス**: UiState 接尾辞（例: `LoginUiState`, `CandleUiState`）
- **UiEvent クラス**: UiEvent 接尾辞（例: `LoginUiEvent`）
- **DTO クラス**: Dto / Request / Response 接尾辞（例: `CandleDto`, `LoginRequest`）
- **Composable 関数**: PascalCase（例: `LoginScreen`, `SymbolItem`）
- **テストクラス**: Test 接尾辞（例: `LoginViewModelTest`）
- **テスト関数名**: バッククォート記法で "action - expected result" 形式

### Step 5: コード品質

以下の観点で品質を検査する：

- **Null安全性**: 非null表明演算子（!!）の不必要な使用がないか、安全呼び出し（?.let / ?:）で処理しているか
- **ディスパッチャーの使用**: `Dispatchers.IO`/`Dispatchers.Main` を直接使わず、`DispatcherProvider` 経由（`dispatcherProvider.io`, `dispatcherProvider.main`）を使用しているか。`GlobalScope` を使っていないか
- **コルーチンパターン**: `viewModelScope.launch(dispatcherProvider.main)` + `withContext(dispatcherProvider.io)` の形式で使用しているか
- **エラーハンドリング**: runCatching/onSuccess/onFailure パターンを使用しているか。例外を無視（空のcatch）していないか。ErrorHandler.logError() と ErrorHandler.mapErrorToResource() を使っているか
- **状態管理**: `MutableStateFlow` が `private` で、`StateFlow` のみが公開されているか。`MutableSharedFlow` も同様に `asSharedFlow()` で公開しているか
- **不要な公開**: `internal` や `private` にすべきクラス・関数が `public` になっていないか
- **コードの重複**: 同じ処理が複数箇所に書かれていないか
- **関数の長さ**: 1つの関数が長すぎないか（目安: 50行以上は要検討）
- **Compose のベストプラクティス**: 副作用が `LaunchedEffect`/`SideEffect` 内で処理されているか、Composable 内で直接コルーチンを起動していないか
- **リソース参照**: ハードコードされた文字列がないか（R.string を使用すべき）

### Step 6: テスト品質（テストファイルが含まれる場合）

`/test-generate` スキル（`.claude/skills/test-generate/SKILL.md`）のルールに基づき、以下の重要な点を確認する：

- **テスト基盤**: `MainDispatcherRule` + `TestDispatcherProvider(mainRule.scheduler)` が正しく使われているか
- **MockK の使い方**: suspend 関数に `coEvery`/`coVerify` が使われているか、`@After` で `confirmVerified()` が呼ばれているか
- **コルーチンテスト**: `runTest(mainRule.scheduler)` が使われているか、非同期操作の後に `advanceUntilIdle()` が呼ばれているか
- **テストケースの網羅性**: 正常系・異常系・境界値がカバーされているか
- **Robolectric**: R.string を参照するコードのテストでのみ @RunWith(RobolectricTestRunner::class) が付与されているか

### Step 7: レビュー結果の出力

以下のフォーマットで結果を出力する。問題がない場合もその旨を明記する：

```text
## レビュー結果

対象: <N>ファイル (+<追加行数>, -<削除行数>)

### 問題点（要修正）
問題がある場合、ファイル名と行番号を添えて具体的に指摘する。

### 改善提案（任意）
必須ではないが、コード品質向上のための提案があれば記載する。

### 良い点
良いコードがあれば積極的に言及する。
```

## 注意事項

- レビューは日本語で行う
- 指摘には必ずファイル名と該当箇所を含める
- 修正案がある場合は具体的なコード例を示す
- 過度に細かい指摘（スタイルの好みレベル）は避け、実質的な品質改善に焦点を当てる
- このスキルはコードの変更は行わない（レビューのみ）
