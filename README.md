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

## 아키텍처
<img width="800" src="https://github.com/user-attachments/assets/34b5d0a5-5921-4926-908d-582d7a630f3b" />

<br><br>

## 기술 스택

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

## 음악 데이터, 편하게 받아올 순 없을까?

게시글에는 사용자가 공유하고 싶은 음악이 함께 담기도록 기획했습니다. <br>
음악 데이터로는 **노래 제목, 가수 이름, 앨범 커버 이미지**가 필요했는데, 이를 매번 직접 입력하게 하면 "4초 영상을 빠르게 올린다"는 흐름이 끊긴다고 판단했습니다. <br>
그래서 **음악 데이터를 최소한의 조작으로 등록할 수 있는 방식**을 고민했고, `검색`과 `실시간 감지` 두 가지 경로를 만들었습니다.

<br>

### Spotify API 연동

먼저 떠올린 것은 외부 API를 활용한 검색 기능이었습니다. 후보는 세 가지였습니다.

<br>

|      API       |                       검토 결과                        |
|:--------------:|:--------------------------------------------------:|
|  Apple Music   |             키 발급에 유료 Apple 개발자 계정이 필수              |
|  YouTube Data  |        앨범 아트가 아닌 동영상 썸네일을 제공하고, 하루 할당량이 부족         |
|  **Spotify**   |      국내 음원 데이터가 풍부하고, **필요한 세 가지 데이터를 모두 제공**      |

<br>

그래서 Spotify의 검색 API를 사용하기로 결정했습니다. <br>
인증은 사용자 로그인이 필요 없는 `Client Credentials` 방식을 사용했습니다. <br>
발급받은 토큰은 만료 시각과 함께 메모리에 캐싱하고, 그럼에도 `401`이 내려오면 토큰을 재발급해 동일 요청을 한 번 더 시도하도록 했습니다.

> 관련 코드 : <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/data/src/main/java/com/fourbeat/data/datasource/spotify/SpotifyRemoteDataSource.kt">SpotifyRemoteDataSource.kt</a>

<br>

**Spotify 검색 API 응답 명세 요약**

```json
{
	"tracks": {
		"next": "https://api.spotify.com/v1/search?query=sugar&type=track&offset=10&limit=10",
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

<br>

응답의 `next`는 **다음 페이지를 가리키는 완성된 URL**입니다. <br>
offset을 직접 계산하는 대신, 이 URL을 Paging3의 key로 그대로 사용하는 커서 방식을 택했습니다. <br>
첫 요청만 검색어와 limit으로 호출하고, 이후 페이지는 `next` URL을 그대로 호출합니다. <br>
검색 화면은 위로 거슬러 올라가며 로드할 일이 없어, `prevKey`는 항상 `null`로 두었습니다.

> 관련 코드 : <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/presentation/src/main/java/com/fourbeat/presentation/ui/main/selectsong/SongSearchPagingSource.kt">SongSearchPagingSource.kt</a>

<br>

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

<br>

사용자가 입력한 검색어를 바탕으로 음악을 검색하도록 했습니다.

- `distinctUntilChanged` : UiState에는 검색어 외에 선택한 곡 같은 값도 함께 들어있어, **검색어가 실제로 바뀐 경우에만** 아래로 흘려보내도록 했습니다.
- `debounce(300L)` : 입력이 멈추고 0.3초가 지나야 방출되도록 해, 타이핑 중 발생하는 불필요한 API 호출을 줄였습니다.
- `filter` : 검색어가 비어 있으면 요청하지 않도록 걸렀습니다.
- `flatMapLatest` : 새로운 검색어가 들어오면 **이전 검색의 Pager flow를 취소**하고 최신 결과만 구독하도록 했습니다.
- `cachedIn` : 화면 회전이나 재구독 시 같은 페이지를 다시 요청하지 않도록 ViewModel 범위에 캐싱했습니다.

> 관련 코드 : <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/presentation/src/main/java/com/fourbeat/presentation/ui/main/selectsong/SelectSongViewModel.kt">SelectSongViewModel.kt</a>

<br>

### 실시간 음악 감지

검색조차 번거로울 수 있다고 생각해, **지금 듣고 있는 음악을 그대로 가져오는** 경로를 추가했습니다.

<br>

안드로이드는 재생 중인 미디어의 메타데이터를 `MediaSessionManager`로 노출합니다. <br>
다만 활성 세션 목록을 얻으려면 **알림 접근 권한을 가진 `NotificationListenerService`** 를 함께 넘겨야 해서, 매니페스트에 빈 서비스를 등록해두고 그 컴포넌트를 전달했습니다. <br>
재생 중인 세션의 `MediaController`에 콜백을 등록해 곡이 바뀔 때마다 제목, 가수, 앨범 아트 URI를 받아오고, 세션이 교체되면 이전 콜백을 해제한 뒤 새 컨트롤러에 다시 등록하도록 했습니다.

> 관련 코드 : <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/data/src/main/java/com/fourbeat/data/datasource/media/MediaSessionLocalDataSource.kt">MediaSessionLocalDataSource.kt</a>, <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/data/src/main/java/com/fourbeat/data/media/MediaNotificationListenerService.kt">MediaNotificationListenerService.kt</a>

<br>

이 방식으로 상위 8개 음악 앱을 대상으로 테스트했습니다.

<img width="600" src="https://github.com/user-attachments/assets/cf6493d2-7bca-4492-a33d-50fb88e3a5e9" />

<br>

테스트를 통해 두 가지 제약사항을 확인했습니다.

1. 알림 접근 권한은 일반 런타임 권한과 달리, **시스템 설정 화면에서 직접 허용**해야 함
2. 일부 앱에선 **앨범 커버 이미지를 제공하지 않음**

<br>

**권한 상태와의 결합**

```kotlin
val liveSongFlow: StateFlow<LiveSongUiState> =
        getLiveSongPermissionFlowUseCase()
            .flatMapLatest { granted ->
                if (!granted) {
                    flowOf(LiveSongUiState.PermissionRequired)
                } else {
                    getMediaSongFlowUseCase().map { song ->
                        if (song != null) LiveSongUiState.Live(song)
                        else LiveSongUiState.None
                    }
                }
            }
            .catch { emit(LiveSongUiState.None) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = LiveSongUiState.Loading,
            )
```

<br>

권한 상태와 실시간 음악 정보는 각각 따로 조회할 수도 있지만, **둘 중 무엇이 바뀌든 화면이 알아서 따라오도록** 만들고 싶었습니다. <br>
그래서 두 데이터를 `callbackFlow`로 감싸 각각 Flow로 제공하고, 하나의 스트림으로 묶었습니다. <br>
권한은 시스템 설정 화면에서 바뀌므로 `ContentObserver`로 설정 값을 관찰했고, 음악 정보는 `MediaController.Callback`으로 받아 흘려보냈습니다. <br>
두 Flow는 `flatMapLatest`로 결합해 **권한이 없으면 세션을 아예 구독하지 않도록** 했고, UiState를 `Loading`, `PermissionRequired`, `None`, `Live` 네 가지로 정의해 화면이 상태만 보고 그려지도록 했습니다. <br>
또 `WhileSubscribed(5_000L)`로 화면을 벗어나고 5초가 지나면 구독을 취소해, 등록했던 콜백이 해제되도록 했습니다.

> 관련 코드 : <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/data/src/main/java/com/fourbeat/data/datasource/media/NotificationListenerPermissionLocalDataSource.kt">NotificationListenerPermissionLocalDataSource.kt</a>, <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/presentation/src/main/java/com/fourbeat/presentation/ui/main/selectsong/SelectSongUiState.kt">SelectSongUiState.kt</a>

<br>

**노래 유사도 알고리즘 적용**

```kotlin
class GetMediaSongFlowUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val searchSongPageUseCase: SearchSongPageUseCase,
    private val resolveBestMatchSongUseCase: ResolveBestMatchSongUseCase,
) {
    operator fun invoke(): Flow<Song?> =
        mediaRepository
            .getSongMetaFlow()
            .map { songMeta ->
                songMeta?.let {
                    Song(
                        title = it.title,
                        artist = it.artist,
                        albumImageUrl = it.albumImageUrl
                            ?: resolveBestAlbumImage(it.title, it.artist)
                    )
                }
            }

    private suspend fun resolveBestAlbumImage(title: String, artist: String): String? {
        val candidates = searchSongPageUseCase(query = title, limit = 10)
            .getOrNull()?.songs ?: return null
        return resolveBestMatchSongUseCase(
            targetTitle = title,
            targetArtist = artist,
            candidates = candidates
        )?.albumImageUrl ?: candidates.firstOrNull()?.albumImageUrl
    }
}
```

<br>

일부 음악 앱에서 앨범 커버 이미지를 제공하지 않는 문제에 대응하기 위해, **앨범 아트가 없을 때만** Spotify 검색으로 이미지를 보완하도록 했습니다. <br>
처음엔 노래 제목으로 검색한 첫 번째 결과를 사용했는데, 같은 제목의 노래가 두 개 이상일 경우 엉뚱한 이미지가 붙었습니다. <br>
예를 들어 SG워너비의 `Timeless`를 검색하면 The Weeknd의 `Timeless`가 선택됐습니다.

> 관련 코드 : <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/domain/src/main/java/com/fourbeat/domain/usecase/media/GetMediaSongFlowUseCase.kt">GetMediaSongFlowUseCase.kt</a>

<br>

그래서 **제목과 가수 이름을 함께 비교하는 유사도 알고리즘**을 적용해, 검색 결과 중 가장 잘 맞는 곡을 고르도록 했습니다.

1. **정규화** : 소문자로 바꾸고 괄호 안 부가 정보(`(Feat. …)`, `(Inst.)`, `[Official MV]`)와 특수문자·공백을 제거해, 앱마다 다른 표기 차이를 없앴습니다.
2. **유사도 계산** : DP 기반의 `최소 편집 거리(레벤슈타인 거리)`로 제목과 가수의 유사도를 각각 0.0 ~ 1.0으로 구하고, 두 점수를 평균냈습니다.
3. **점수 보정** : 한쪽이 다른 쪽을 포함하는 경우(`BTS` ↔ `BTS feat. Halsey`) 편집 거리만으로는 점수가 크게 깎이기 때문에, 최소 점수를 보장하도록 보정했습니다.
4. **임계값** : 평균 점수가 `0.4`에 못 미치는 후보는 모두 제외해, 맞는 곡이 없으면 차라리 이미지를 붙이지 않도록 했습니다.

<br>

피처링, 괄호 제목, `Inst.` 버전, 동명이곡 같은 경계 케이스는 유닛 테스트로 검증했습니다.

> 관련 코드 : <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/domain/src/main/java/com/fourbeat/domain/usecase/music/ResolveBestMatchSongUseCase.kt">ResolveBestMatchSongUseCase.kt</a>, <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/domain/src/test/java/com/fourbeat/domain/usecase/music/ResolveBestMatchSongUseCaseTest.kt">ResolveBestMatchSongUseCaseTest.kt</a>

<br>

## 큰 영상 데이터, 어떻게 다뤄야할까?

게시글에는 4초의 영상을 포함할 수 있도록 기획했습니다. <br>
영상은 `CameraX`로 촬영하고, `Pre-signed URL` 방식으로 스토리지에 직접 업로드하기로 결정했습니다. <br>
다만 영상은 텍스트나 이미지와 비교할 수 없이 큰 데이터라, **전송 시간과 스토리지 비용**을 어떻게 줄일지 고민이 필요했습니다.

<br>

### 빠른 전송을 위한 영상 크기 조절

먼저 떠올린 것은 영상의 크기를 조절하는 것이었습니다. <br>
UHD(2160p)로 촬영한 4초 영상의 크기는 **75.2MB**였고, 이대로라면 전송 지연과 스토리지 비용 문제가 예상됐습니다. <br>
그래서 해상도와 비트레이트, 통신 환경을 바꿔가며 네 가지 케이스를 테스트했습니다.

<br>

|        Case        | File 크기 | Wifi 환경 (평균 390KB/s) |  LTE 환경 (평균 147KB/s)   |
|:------------------:|:-------:|:--------------------:|:----------------------:|
|    UHD (2160p)     | 75.2MB  |   OutOfMemoryError   |    OutOfMemoryError    |
|     HD (720p)      | 5.56MB  |        14.33s        | SocketTimeoutException |
|     SD (480p)      | 2.10MB  |        5.73s         |         7.22s          |
| SD + 비트레이트 (1Mbps) | 1.05MB  |        2.64s         |         7.22s          |

<br>

UHD 해상도는 두 환경 모두 `OutOfMemoryError`가 발생했고, HD 해상도는 LTE 환경에서 타임아웃이 발생해 제외했습니다. <br>
결과적으로 **SD 해상도 + 1Mbps 비트레이트** 조합에서 파일 크기는 1.05MB로 원본의 약 1/70 수준까지, Wifi 환경의 전송 시간은 2.64초까지 줄일 수 있었습니다.

<br>

```kotlin
val videoCapture = remember(uiState.cameraLens) {
    val recorder = Recorder.Builder()
        .setQualitySelector(QualitySelector.from(Quality.SD))
        .setTargetVideoEncodingBitRate(1_000_000)
        .build()
    VideoCapture.withOutput(recorder)
}
```

<br>

크기 조절은 촬영이 끝난 뒤 다시 인코딩하는 대신, CameraX의 `Recorder` 설정으로 **촬영 단계에서부터 작게 찍도록** 처리했습니다. <br>
후처리 인코딩은 그만큼 사용자를 더 기다리게 만들고, 4초 영상은 피드에서 작게 재생되므로 높은 화질을 유지할 이유도 없다고 판단했습니다. <br>
촬영된 파일은 업로드가 끝나면 필요 없어지는 임시 파일이라, 갤러리를 건드리지 않고 OS가 정리할 수 있는 앱의 `cacheDir`에 저장했습니다.

> 관련 코드 : <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/presentation/src/main/java/com/fourbeat/presentation/ui/main/camera/CameraScreen.kt">CameraScreen.kt</a>, <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/presentation/src/main/java/com/fourbeat/presentation/ui/main/camera/CameraViewModel.kt">CameraViewModel.kt</a>

<br>

하지만 LTE 환경에선 파일 크기를 절반으로 줄여도 전송 시간이 **7.22초에서 더 줄지 않는** 문제가 있었습니다. <br>
파일이 작아질수록 전체 소요 시간에서 실제 데이터를 실어 보내는 시간의 비중은 작아지고, 연결 수립과 초기 전송 구간에서 왕복하는 **RTT의 비중이 커지기** 때문이었습니다. <br>
이 구간은 대역폭이 아니라 지연 시간에 묶여 있어, 파일 크기를 더 줄이는 것만으로는 해결되지 않았습니다. <br>
결국 **전송 시간을 줄이는 대신, 사용자가 전송을 기다리지 않게 만드는** 방향이 필요했습니다.

<br>

### 백그라운드 작업 예약으로 영상 전송 보장

LTE 환경에서 전송 시간을 줄이는 데 한계가 있다는 점을 `백그라운드 작업 예약`과 `낙관적 UI 업데이트`로 극복하고자 했습니다. <br>
또한 게시글 업로드가 하루 4번으로 제한된 서비스에서, 영상 전송 중 네트워크 끊김이나 앱 이탈로 촬영한 영상이 날아가는 것은 치명적이라 **전송 보장**이 필요했습니다. <br>
그래서 `Room DB`를 게시글 데이터의 단일 저장소로 삼고, `WorkManager`로 영상을 백그라운드에서 전송한 뒤 업데이트하는 방식으로 설계했습니다.

<br>

```kotlin
class SubmitPostUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
    private val workRepository: WorkRepository,
    private val preferenceRepository: PreferenceRepository,
) {
    suspend operator fun invoke(
        groupId: Long,
        request: CreatePostRequest,
        videoFileInfo: VideoFileInfo?,
    ): Result<Unit> = runCatching {
        val member = preferenceRepository.getUser()

        val tempId = groupRepository.insertOptimisticPost(
            groupId = groupId,
            member = member,
            request = request,
            filePath = videoFileInfo?.file?.absolutePath,
        )

        workRepository.enqueueCreatePost(
            groupId = groupId,
            tempId = tempId,
            request = request,
            videoFileInfo = videoFileInfo,
        )
    }
}
```

<br>

사용자가 게시글 작성을 요청한 후 과정은 다음과 같습니다.

1. 게시글 데이터를 Room DB에 낙관적 업데이트, 영상 경로는 로컬 경로로 지정
2. 발급된 임시 id로 WorkManager를 통해 영상 전송 작업을 예약
3. pre-signed url 발급, 스토리지에 영상 파일 업로드, 반환된 영상 url과 함께 게시글 작성 요청
4. 성공하면 영상 경로를 스토리지 경로로 교체
5. 3회까지 시도한 뒤에도 실패하면 롤백

<br>

임시 id는 서버가 내려주는 id와 겹치지 않도록 항상 음수로 발급해, 피드에서 두 데이터를 같은 목록으로 다룰 수 있게 했습니다. <br>
작업은 `enqueueUniqueWork`로 그룹마다 고유한 이름을 부여해 같은 그룹에 작업이 중복으로 쌓이지 않도록 했고, 실패 시에는 `BackoffPolicy.EXPONENTIAL`로 15초부터 간격을 늘려가며 재시도하도록 했습니다.

> 관련 코드 : <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/domain/src/main/java/com/fourbeat/domain/usecase/group/SubmitPostUseCase.kt">SubmitPostUseCase.kt</a>, <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/data/src/main/java/com/fourbeat/data/repository/WorkRepositoryImpl.kt">WorkRepositoryImpl.kt</a>, <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/data/src/main/java/com/fourbeat/data/worker/CreatePostWorker.kt">CreatePostWorker.kt</a>

<br>

<img width="1000" src="https://github.com/user-attachments/assets/e4a0c396-f755-48da-9fd0-87de84f8630c" />

<br>

사용자는 게시글 작성 버튼을 누르는 즉시 대기 시간 없이 게시글을 확인할 수 있게 됐습니다. <br>
또한 `NetworkType.CONNECTED` 제약을 걸어둔 덕분에, 위처럼 네트워크가 끊겼다가 다시 연결된 상황(비행기 모드)에서도 재시도를 통해 영상 전송부터 게시글 등록까지 성공하는 것을 확인할 수 있었습니다.

<br>

### 부드러운 자동 재생을 위한 캐싱과 인스턴스 관리

피드는 날짜별로 위아래(`VerticalPager`), 그룹 멤버별로 좌우(`HorizontalPager`)로 넘기는 구조입니다. <br>
한 화면에는 3명의 게시글이 세로로 배치되고, 각 영상은 `REPEAT_MODE_ONE`으로 무한 반복 재생되도록 기획했습니다. <br>
**항상 3개의 영상이 동시에, 끝없이 재생되는** 화면이다 보니 세 가지 문제가 차례로 드러났습니다.

<br>

1. 영상이 반복될 때마다 같은 파일을 다시 내려받음
2. 페이지를 넘길수록 ExoPlayer 인스턴스가 늘어나 화면이 버벅임
3. 날짜를 넘길 때 피드 데이터를 새로 불러오느라 스크롤이 끊김

<br>

**1. 디스크 캐싱으로 재다운로드 막기**

<br>

가장 먼저 눈에 띈 것은 **같은 영상이 반복될 때마다 네트워크 요청이 다시 나가는** 문제였습니다. <br>
매번 재생이 지연되는 것도 문제지만, 사용자의 데이터 사용량이 계속 늘어난다는 점이 더 치명적이라고 판단했습니다. <br>
캐시는 디스크 캐시를 선택했습니다. 영상은 메모리에 담기엔 크고, 앱이 종료되면 사라지는 휘발성 때문에 반복 재생의 이득을 살릴 수 없기 때문입니다.

<br>

```kotlin
@Provides
@Singleton
fun provideSimpleCache(@ApplicationContext context: Context): SimpleCache =
    SimpleCache(
        File(context.cacheDir, "4beat_video_cache"),
        LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES), // 200MB
        StandaloneDatabaseProvider(context),
    )

@Provides
@Singleton
fun provideDataSourceFactory(context: Context, cache: SimpleCache): DataSource.Factory =
    DefaultDataSource.Factory(
        context,
        CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR),
    )
```

<br>

- `SimpleCache` : Media3가 제공하는 디스크 캐시입니다. 영상을 한 파일에 통째로 담지 않고 **조각 단위로 나눠 저장**하기 때문에, 파일 전체가 내려오기 전에도 앞부분부터 재생할 수 있습니다. 짧은 영상이 빠르게 넘어가는 피드에 적합하다고 판단했습니다.
- `LeastRecentlyUsedCacheEvictor` : 캐시 상한을 200MB로 두고, 오래 보지 않은 영상부터 지워지도록 했습니다.
- `StandaloneDatabaseProvider` : 어떤 조각이 어디에 저장돼 있는지를 관리하는 인덱스 DB입니다.
- `CacheDataSource` : 캐시에 있으면 디스크에서 읽고, 없으면 네트워크에서 받아온 뒤 캐시에 씁니다. `FLAG_IGNORE_CACHE_ON_ERROR`로 캐시 쓰기가 실패하더라도 재생 자체는 이어지도록 했습니다.

> 관련 코드 : <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/data/src/main/java/com/fourbeat/data/media/di/VideoCacheModule.kt">VideoCacheModule.kt</a>

<br>

**2. 인스턴스 풀링으로 플레이어 개수 고정하기**

<br>

`ExoPlayer`는 코덱과 버퍼를 점유하는 무거운 객체입니다. <br>
슬롯마다 인스턴스를 하나씩 만들면, Pager가 인접 페이지까지 미리 구성하는 특성상 **화면에 보이는 3개보다 훨씬 많은 인스턴스가 동시에 살아있게** 됩니다. <br>
그래서 인스턴스를 **3개로 고정해두고 슬롯 위치에 따라 재활용**하는 방식으로 바꿨습니다.

<br>

```kotlin
@Composable
fun rememberExoPlayerPool(size: Int = 3): List<ExoPlayer> {
    val players = remember {
        List(size) {
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                .build()
                .apply { repeatMode = ExoPlayer.REPEAT_MODE_ONE }
        }
    }
    DisposableEffect(Unit) {
        onDispose { players.forEach { it.release() } }
    }
    return players
}
```

<br>

```kotlin
group.forEachIndexed { index, slot ->
    GroupDetailSlotItem(
        slot = slot,
        isActive = isGroupActive,
        exoPlayer = exoPlayers.getOrNull(index),
    )
}
```

<br>

- 풀은 피드 화면 전체에서 하나만 만들어 **슬롯 위치(0/1/2)에 고정 할당**했습니다. 날짜나 페이지를 넘겨도 인스턴스는 그대로 두고 `MediaItem`만 교체합니다.
- 화면 밖의 페이지에는 `isActive = false`를 내려보내 `pause`시켜, 보이지 않는 영상이 디코딩을 점유하지 않도록 했습니다.
- `DisposableEffect`로 화면을 벗어날 때 세 인스턴스를 모두 `release`해 코덱을 반납하도록 했습니다.

> 관련 코드 : <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/presentation/src/main/java/com/fourbeat/presentation/ui/component/VideoPlayer.kt">VideoPlayer.kt</a>, <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/presentation/src/main/java/com/fourbeat/presentation/ui/main/groupdetail/GroupDetailScreen.kt">GroupDetailScreen.kt</a>

<br>

**3. 인접 날짜 프리페치로 스크롤 이어 붙이기**

<br>

날짜를 넘길 때마다 피드를 새로 요청하면, 데이터가 도착할 때까지 화면이 비어 스크롤이 끊깁니다. <br>
숏폼 서비스처럼 **넘기는 순간 이미 준비돼 있는** 경험을 만들기 위해, `VerticalPager`를 3페이지로 고정하고 가운데를 항상 현재 날짜로 유지하는 방식을 택했습니다.

<br>

```kotlin
val pagerState = rememberPagerState(initialPage = 1) { 3 }

// 날짜가 바뀌면 다시 가운데로 복귀
LaunchedEffect(uiState.currentFeed?.date) {
    if (pagerState.currentPage != 1) pagerState.scrollToPage(1)
}

LaunchedEffect(pagerState.settledPage) {
    when (pagerState.settledPage) {
        2 -> onEvent(GroupDetailEvent.OnScrollToPrev)
        0 -> onEvent(GroupDetailEvent.OnScrollToNext)
    }
}
```

<br>

```kotlin
private fun scrollToPrev() {
    val prev = uiState.value.previousFeed ?: return
    adjacentFeeds.value = null to uiState.value.currentFeed
    currentDate.value = prev.date

    scrollJob?.cancel()
    scrollJob = viewModelScope.launch {
        refreshGroupFeedUseCase(groupId, prev.date)
        prev.previousDate?.let { prefetch(it, isPrevious = true) }   // 다음에 올 날짜를 미리 확보
    }
}
```

<br>

- 페이지 `0 / 1 / 2`를 각각 **다음 날짜 / 현재 날짜 / 이전 날짜**에 대응시켰습니다. 사용자는 계속 스크롤하지만 Pager는 항상 3페이지만 들고 있습니다.
- 현재 날짜를 불러올 때 **앞뒤 날짜를 함께 요청**해두기 때문에, 스크롤이 도착한 시점에는 이미 데이터가 준비된 상태입니다. 이동이 끝나면 그 다음 날짜를 다시 프리페치해 항상 양옆이 채워지도록 했습니다.
- 인접한 날짜가 없으면 `NestedScrollConnection`으로 스크롤 자체를 막아, 빈 화면이 노출되지 않도록 했습니다.

> 관련 코드 : <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/presentation/src/main/java/com/fourbeat/presentation/ui/main/groupdetail/GroupDetailViewModel.kt">GroupDetailViewModel.kt</a>, <a href="https://github.com/SeungWoo-Ahn/4Beat-Android/blob/main/presentation/src/main/java/com/fourbeat/presentation/ui/main/groupdetail/GroupDetailScreen.kt">GroupDetailScreen.kt</a>
