package org.carlspring.strongbox.security;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 */
@XmlRootElement(name = "repositories")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserRepositories
{

    @XmlElement(name = "repository")
    private Set<UserRepository> repositories = new LinkedHashSet<>();


    public UserRepositories()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRepositories that = (UserRepositories) o;
        return Objects.equal(repositories, that.repositories);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(repositories);
    }

    public Set<UserRepository> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(Set<UserRepository> repositories)
    {
        this.repositories = repositories;
    }


    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Repositories{");
        sb.append("repositories=")
          .append(repositories);
        sb.append('}');
        return sb.toString();
    }
}
