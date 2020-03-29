import javax.inject.Inject
import org.gradle.api.model.ObjectFactory

open class DownloadExtension @Inject constructor(objectFactory: ObjectFactory) {
    // Use an injected ObjectFactory to create a nested Resource object
    val resource: Resource = objectFactory.newInstance(Resource::class.java)
}
