<img width="100" alt="4beat-logo" src="https://github.com/user-attachments/assets/bb759c88-aeb2-4e15-b40c-f54911b4cd92">

## 4Beat

**4Beat**는 음악과 일상을 공유하는 그룹 기반의 SNS 서비스입니다. <br>
하루에 4번, 4초의 영상으로 그 순간을 원하는 사람들과 공유할 수 있도록 기획했습니다. <br>
팀 프로젝트로 2인 팀에서 기획 및 Android 앱 개발을 담당했습니다. <br><br>

### 한 눈에 보기

| <img width="240" src="https://github.com/user-attachments/assets/60e4d9bb-243d-4011-b379-e643537c9140"> | <img width="240" src="https://github.com/user-attachments/assets/ac7a6b27-51bd-466d-8118-d80e3256deb3"> | <img width="240" src="https://github.com/user-attachments/assets/24753a8e-de74-4ee6-9c7d-eb441ee94ded"> |
|:------------------------:|:------------------------:|:------------------------:|
|          그룹 참여           |            피드            |        Spotify 검색        |

| <img width="240" src="https://github.com/user-attachments/assets/ae008003-ae10-4d90-a21f-8274758c4929"> | <img width="240" src="https://github.com/user-attachments/assets/2f194fa2-6259-431f-ae30-d667c1de2239"> | <img width="240" src="https://github.com/user-attachments/assets/5c6519d1-22cd-417c-a05f-3a7e93cc57b9">  |
|:------------------------:|:------------------------:|:-------------------------:|
|        실시간 노래 감지         |          영상 촬영           |          게시글 작성           |


<br><br>

### 아키텍처
<img width="800" src="https://github.com/user-attachments/assets/34b5d0a5-5921-4926-908d-582d7a630f3b" />

<br><br>

### 기술 스택

|           카테고리           |                                           스택                                           |
|:------------------------:|:--------------------------------------------------------------------------------------:|
|       **Language**       |                                         Kotlin                                         |
|     **Asynchronous**     |                                    Coroutines, Flow                                    |
| **Project Architecture** |                          Clean App Architecture, Multi Module                          |
|       **Jetpack**        | Compose, ViewModel, Navigation, DataStore, Paging3, Room, WorkManager, CameraX, Media3 |
|          **DI**          |                                          Hilt                                          |
|       **Network**        |                                          Ktor                                          |
|         **ETC**          |                                  Timber, Kakao OAuth                                   |

<br><br>

### 음악 데이터, 편하게 받아올 순 없을까?

사용자가 공유하고 싶은 음악을 게시글에 포함하도록 기획했습니다. <br>
음악 데이터로는 노래 제목, 가수 이름, 앨범 커버 이미지가 필요했습니다. <br>
그래서 사용자가 이 **음악 데이터를 편하게 등록할 수 있는 방식**을 고민했습니다. <br><br>


#### Spotify API 연동

처음으로 떠올린 것은 외부 API를 활용한 검색 기능이었습니다. <br>
후보군에는 `Spotify`, `Apple Music`, `YouTube Data`의 API를 사용할 수 있었습니다. <br>
Apple Music는 Apple 개발자 계정이 필수이고, YouTube Data는 앨범 아트가 아닌 동영상 썸네일을 제공하고 하루 할당량이 적다는 한계가 있었습니다. <br>
그래서 국내 음원 데이터가 풍부하면서, 필요한 데이터를 모두 제공하는 Spotify의 API를 사용하기로 결정했습니다. <br>

**Spotify 검색 API 응답 명세 요약**
```json
{
	"tracks": {
		"next": "https://api.spotify.com/v1/me/shows?offset=1&limit=1",
		"previous": "https://api.spotify.com/v1/me/shows?offset=1&limit=1",
		"items": [
			{
				"name": "sugar",
				"album": {
					"release_date": "2005-12",
					"images": [
						{
							"url": "https://i.scdn.co/image/ab67616d00001e02ff9ca10b55c",
							"width": 300,
							"height": 300
						}
					]
				},
				"artists": [
					{ "name": "Maroon5" }
				]
			}
		]
	}
}
```
응답 명세의 `next`와 `previous` URL을 Paging3 key로 활용한 무한 스크롤 기능을 구현하기로 결정했습니다. <br>

```kotlin
val songPagingFlow: Flow<PagingData<Song>> =
        _uiState
            .map { it.searchQuery }
            .distinctUntilChanged()
            .debounce(300L)
            .filter { it.isNotBlank() }
            .flatMapLatest { query ->
                Pager(
                    config = PagingConfig(pageSize = 10, initialLoadSize = 10),
                    pagingSourceFactory = { SongSearchPagingSource(query, searchSongPageUseCase) },
                ).flow
            }
            .cachedIn(viewModelScope)
```
사용자가 입력한 검색어를 바탕으로 음악을 검색하도록 했습니다. <br>
`distinctUntilChanged`로 검색어가 변경되지 않을 때, 새로운 flow가 나가지 않도록 방지했고, <br>
`debounce`에 300ms를 두어 입력 중인 경우에도 불필요한 API 호출을 줄였습니다. <br>
또 `flatMapLatest`를 통해 상위 flow에서 새로운 값이 들어오면, 기존 flow를 취소하고 최신값을 전달하도록 구성했습니다.

<br>

#### 실시간 음악 감지

<br>

### 큰 영상 데이터, 어떻게 다뤄야할까?

#### 빠른 전송을 위한 영상 크기 조절

<br>

#### 백그라운드 작업 예약으로 영상 전송 보장

<br>

#### 자동 재생을 위한 영상 캐싱

<br>

#### 플레이어 인스턴스 조절

<br>
