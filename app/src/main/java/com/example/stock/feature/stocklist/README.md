# 銘柄一覧機能

## 概要

`stocklist`機能モジュールは、APIから取得した銘柄リストを表示する。
データ層、UI層、ViewModel層を明確に分離したMVVMアーキテクチャパターンに従う。

### 主な機能

- **銘柄リスト表示**: 銘柄名とコードを含むリストを表示
- **ローディング状態**: データ取得中にプログレスインジケータを表示
- **エラーハンドリング**: 失敗時にリトライボタン付きのエラーメッセージを表示
- **空状態**: 銘柄がない場合にメッセージを表示
- **ナビゲーション**: 銘柄をタップするとチャート画面に遷移

## 依存関係図

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              UI層                                        │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                     SymbolListScreen                            │    │
│  │  ┌─────────────────────────────────────────────────────────┐    │    │
│  │  │              SymbolListScreenContent                    │    │    │
│  │  │  ┌───────────────┐                                      │    │    │
│  │  │  │ SymbolUiState │                                      │    │    │
│  │  │  └───────────────┘                                      │    │    │
│  │  └─────────────────────────────────────────────────────────┘    │    │
│  └──────────────────────────────┬──────────────────────────────────┘    │
└─────────────────────────────────┼───────────────────────────────────────┘
                                  │ observes
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           ViewModel層                                    │
│                    ┌─────────────────────┐                              │
│                    │   SymbolViewModel   │                              │
│                    │  - ui: StateFlow    │                              │
│                    │  - load()           │                              │
│                    └──────────┬──────────┘                              │
└───────────────────────────────┼─────────────────────────────────────────┘
                                │ calls
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          Repository層                                    │
│                    ┌─────────────────────┐                              │
│                    │  SymbolRepository   │                              │
│                    │  - fetchSymbols()   │                              │
│                    └─────────┬───────────┘                              │
└──────────────────────────────┼──────────────────────────────────────────┘
                               │ depends on
                               ▼
                    ┌─────────────────────┐
                    │     SymbolApi       │
                    │    (Retrofit)       │
                    │  - getSymbols()     │
                    └─────────┬───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │   バックエンドAPI     │
                    │   GET /symbols      │
                    └─────────────────────┘
```

## ディレクトリ構成

```
feature/stocklist/
├── data/
│   ├── remote/
│   │   ├── SymbolApi.kt        # 銘柄エンドポイント用Retrofitインターフェース
│   │   └── SymbolDto.kt        # 銘柄データ用DTO
│   └── repository/
│       └── SymbolRepository.kt # APIから銘柄リストを取得
│
├── domain/
│   └── model/
│       └── Symbol.kt           # 銘柄ドメインエンティティ
│
├── ui/
│   ├── SymbolListScreen.kt     # Hilt ViewModel注入を使用した銘柄リストComposable
│   ├── SymbolItem.kt           # 銘柄リストアイテムのComposable
│   └── SymbolUiState.kt        # 銘柄リスト画面の状態データクラス
│
├── viewmodel/
│   └── SymbolViewModel.kt      # 銘柄リストの状態とビジネスロジックを管理
│
└── README.md                   # このファイル
```

## テスト

### テストの場所

テストは以下に配置：

| テスト種類 | 場所                                                      |
|----------|----------------------------------------------------------|
| 単体テスト | `app/src/test/java/com/example/stock/feature/stocklist/` |

### テストファイル

#### 単体テスト

| ファイル                                        | 説明                                           |
|------------------------------------------------|-----------------------------------------------|
| `viewmodel/SymbolViewModelTest.kt`             | ロード成功/失敗を含むSymbolViewModelのテスト     |
| `data/repository/SymbolRepositoryTest.kt`      | SymbolRepositoryのAPI呼び出しテスト             |

### テストの実行

```bash
# stocklist機能の全単体テストを実行
./gradlew testDebugUnitTest --tests "*.SymbolViewModelTest.*"
./gradlew testDebugUnitTest --tests "*.SymbolRepositoryTest.*"
```

### テストカバレッジ

#### SymbolViewModelテスト

- ロード成功 - 銘柄を更新しエラーをクリア
- ロード失敗 - エラーを設定し銘柄は変更なし
- ロードは新しいリクエスト開始時に以前のエラーをクリア

#### SymbolRepositoryテスト

- fetchSymbolsはAPIから銘柄を返す

### テストパターン

この機能では以下のテストパターンを使用：

1. **MainDispatcherRule**: 決定論的なコルーチンテストのため`Dispatchers.Main`を`StandardTestDispatcher`に置換
2. **MockK**: シンプルなモック用に`relaxed = true`を使用するモックフレームワーク
3. **Truth**: `assertThat()`スタイルを使用するアサーションライブラリ

テストパターンの例：

```kotlin
@Test
fun `load success - updates symbols and clears error`() = runTest(mainRule.scheduler) {
    // given
    val expected = listOf(
        SymbolDto("AAPL", "Apple Inc."),
        SymbolDto("GOOG", "Alphabet Inc.")
    )
    coEvery { repo.fetchSymbols() } returns expected

    // when
    vm.load()
    advanceUntilIdle()

    // then
    val state = vm.ui.value
    assertThat(state.isLoading).isFalse()
    assertThat(state.error).isNull()
    assertThat(state.symbols).isEqualTo(expected)
}
```
