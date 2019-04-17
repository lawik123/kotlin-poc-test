package nl.lawik.poc.multiplatform.util

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder

object HibernateUtil {
    val sessionFactory: SessionFactory by lazy {
        val standardRegistry = StandardServiceRegistryBuilder().configure().build()
        val metaData = MetadataSources(standardRegistry).metadataBuilder.build()
        metaData.sessionFactoryBuilder.build()
    }
}

/**
 * Helper for creating a session scope and provides it to the provided function literal.
 * The session is closed after the function literal is processed
 * the result from the function literal is returned.
 *
 * @param handler function literal that makes use of the session, you can access the session using the "it" keyword
 * in the function literal.
 *
 * @return the result of the function literal
 */
fun <T> openAndCloseSession(handler: (Session) -> T): T {
    val session = HibernateUtil.sessionFactory.openSession()
    val res = handler(session)
    if (session.isOpen) {
        session.close()
    } else {
        println("[WARNING]: session closed by handler function: ${handler::class.java.enclosingMethod}, please consider omitting manually closing the session in the handler as it will be closed automatically at the end of the call")
    }
    return res
}