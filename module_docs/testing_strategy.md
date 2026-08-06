Sub-task 1: Testing strategy

Unit tests
The cheapest and fast tests. In my case I would cover with unit tests all microservices classes which are used
for data transformation / validation / business logic (validators, mappers, extractors) with mocks.
Service-layer business logic also should be covered with unit tests when there is real decision logic
(for example SongServiceImpl). Thin orchestration with almost no logic I would skip or keep minimal.

Resource Service:
Mp3Validator
ContentTypeValidator
IdsParameterParser
IdValidator
S3ExceptionMapper
StorageKeyGenerator
ResourceUploadedPublisher — with mocked StreamBridge (send success / false / broker exception).
This class is thin, so a heavy Rabbit Testcontainers / Stream test-binder test gives little extra value
compared to a simple unit test.

ResourceController — as a slice/unit-style test with mocked service (for example @WebMvcTest / MockMvc),
not as a pure class test without Spring.

ResourceServiceImpl could be covered with unit tests as well, but in my opinion in current
implementation we are not getting much value, because there is small amount of business logic and a big amount of integrations.


Song Service:
IdsParameterParser
IdValidator
SongMapper
SongServiceImpl

SongController — same idea as ResourceController: API slice with mocked service.

Resource Processor:
SongMetadataMapper
Mp3MetadataExtractor


Integration tests
For integration tests I would cover Resource service + S3 Storage with Testcontainers (LocalStack).
In my opinion this is the critical non-trivial integration area: real S3 client against a real S3 API.

About Resource service + RabbitMQ: Stream test-binder and Rabbit Testcontainers for ResourceUploadedPublisher.
Both setups are heavy for a thin publisher wrapper. So for messaging publish behavior I keep a unit test with mocked StreamBridge.

About DB integration tests: our repositories are mostly simple CRUD (save / findById / delete), without complex
custom queries. So I would deprioritize dedicated DB integration tests for now and rely more on contracts / component / E2E
to check that data is actually persisted. If later we add non-trivial queries, DB integration tests would become more valuable.


Component tests
I think that every microservice needs min one component test.
Dependencies of that service can be stubbed / testcontainers / in-memory where needed, but we still verify
the service behavior end-to-end inside its own boundary. They are very good because they check every microservice in isolation
at business level, not only one class.


Contract tests
Every contract communication should be covered with contract tests (sync HTTP and async messaging).
Cases which I choose to cover: 

Messaging:
Resource-service -> RabbitMQ -> Resource-processor (resource.uploaded / resourceId)

HTTP (implemented with stubs propagation):
Resource-processor -> Resource-service (GET resource binary)

E2E tests
One minimal Cucumber API scenario (happy path):
upload MP3 to Resource Service → wait until Song Service returns metadata for that id.


How this combination helps
Unit tests give fast feedback on validators / mappers / extractors / publisher error handling and catch logic bugs cheaply.
Contract tests protect service-to-service HTTP and messaging agreements.
Integration tests cover the risky wiring where mocks alone are not enough — in our case mainly S3 / LocalStack.
Component tests prove each microservice works as a product through its API for main business scenarios.
A small E2E suite proves the full pipeline in real still works together.

So, finally our picture should be: as much as possible useful unit tests, a lot of contract tests,
less integration tests and component tests, and a little E2E.
