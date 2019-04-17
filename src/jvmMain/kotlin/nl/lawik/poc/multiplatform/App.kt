package nl.lawik.poc.multiplatform

import nl.lawik.poc.multiplatform.endpoint.API_PATH
import nl.lawik.poc.multiplatform.endpoint.API_PORT
import javax.ws.rs.ApplicationPath
import javax.ws.rs.core.Application

@ApplicationPath(API_PATH)
class App : Application()