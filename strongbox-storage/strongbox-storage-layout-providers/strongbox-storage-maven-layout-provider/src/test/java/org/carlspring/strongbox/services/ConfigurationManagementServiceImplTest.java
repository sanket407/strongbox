package org.carlspring.strongbox.services;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.HttpConnectionPool;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RuleSet;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;

import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class ConfigurationManagementServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementServiceImplTest.class);

    private static final String RULE_PATTERN = "*.org.test";

    private static final String REPOSITORY_RELEASES_1 = "cmsi-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "cmsi-releases-2";

    private static final String REPOSITORY_GROUP_1 = "csmi-group-1";

    private static final String REPOSITORY_GROUP_2 = "csmi-group-2";

    private static final String REPOSITORY_4_DB_VERSION_1 = "db-versioned-conf-release-1";

    private static final String REPOSITORY_4_DB_VERSION_2 = "db-versioned-conf-release-2";

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_2));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_4_DB_VERSION_1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_4_DB_VERSION_2));

        return repositories;
    }

    @Before
    public void setUp()
            throws Exception
    {
        Repository repository1 = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_RELEASES_1);
        repository1.setType(RepositoryTypeEnum.HOSTED.getType());

        Repository repository2 = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_RELEASES_2);
        repository2.setType(RepositoryTypeEnum.HOSTED.getType());

        Repository groupRepository1 = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_GROUP_1);
        groupRepository1.setType(RepositoryTypeEnum.GROUP.getType());
        groupRepository1.getGroupRepositories().add(repository1.getId());

        Repository groupRepository2 = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_GROUP_2);
        groupRepository2.setType(RepositoryTypeEnum.GROUP.getType());
        groupRepository2.getGroupRepositories().add(repository1.getId());

        createRepository(repository1);
        createRepository(repository2);
        createRepository(groupRepository1);
        createRepository(groupRepository2);
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    @Test
    public void testGetGroupRepositories()
    {
        List<Repository> groupRepositories = configurationManagementService.getGroupRepositories();

        assertFalse(groupRepositories.isEmpty());

        logger.debug("Group repositories:");

        for (Repository repository : groupRepositories)
        {
            logger.debug(" - " + repository.getId());
        }
    }

    @Test
    public void testGetGroupRepositoriesContainingRepository()
    {
        List<Repository> groups = configurationManagementService.getGroupRepositoriesContaining(STORAGE0,
                                                                                                REPOSITORY_RELEASES_1);

        assertFalse(groups.isEmpty());

        logger.debug("Group repositories containing \"" + REPOSITORY_RELEASES_1 + "\" repository:");

        for (Repository repository : groups)
        {
            logger.debug(" - " + repository.getId());
        }
    }

    @Test
    public void testRemoveRepositoryFromAssociatedGroups()
            throws Exception
    {
        assertEquals("Failed to add repository to group!",
                     2,
                     configurationManagementService.getGroupRepositoriesContaining(STORAGE0,
                                                                                   REPOSITORY_RELEASES_1).size());

        configurationManagementService.removeRepositoryFromAssociatedGroups(STORAGE0, REPOSITORY_RELEASES_1);

        assertEquals("Failed to remove repository from all associated groups!",
                     0,
                     configurationManagementService.getGroupRepositoriesContaining(STORAGE0,
                                                                                   REPOSITORY_RELEASES_1).size());

        configurationManagementService.removeRepository(STORAGE0, REPOSITORY_GROUP_1);
        configurationManagementService.removeRepository(STORAGE0, REPOSITORY_GROUP_2);
    }

    @Test
    public void testSetProxyRepositoryMaxConnections()
            throws IOException, JAXBException
    {
        Storage storage = configurationManagementService.getStorage(STORAGE0);

        Repository repository = storage.getRepository(REPOSITORY_RELEASES_2);

        configurationManagementService.saveRepository(STORAGE0, repository);

        configurationManagementService.setProxyRepositoryMaxConnections(storage.getId(), repository.getId(), 10);

        HttpConnectionPool pool = configurationManagementService.getHttpConnectionPoolConfiguration(storage.getId(),
                                                                                                    repository.getId());

        assertNotNull(pool);
        assertEquals(10, pool.getAllocatedConnections());
    }

    @Test
    public void addAcceptedRuleSet()
            throws Exception
    {
        final RuleSet ruleSet = getRuleSet();
        final boolean added = configurationManagementService.saveAcceptedRuleSet(ruleSet);
        final Configuration configuration = configurationManagementService.getConfiguration();

        final RuleSet addedRuleSet = configuration.getRoutingRules().getAccepted().get(REPOSITORY_GROUP_1);

        assertTrue(added);
        assertNotNull(addedRuleSet);
        assertEquals(1, addedRuleSet.getRoutingRules().size());
        assertTrue(addedRuleSet.getRoutingRules().get(0).getRepositories().contains(REPOSITORY_RELEASES_1));
        assertEquals(1, addedRuleSet.getRoutingRules().get(0).getRepositories().size());
        assertEquals(RULE_PATTERN, addedRuleSet.getRoutingRules().get(0).getPattern());
    }

    @Test
    public void testRemoveAcceptedRuleSet()
            throws Exception
    {
        configurationManagementService.saveAcceptedRuleSet(getRuleSet());

        final boolean removed = configurationManagementService.removeAcceptedRuleSet(REPOSITORY_GROUP_1);

        final Configuration configuration = configurationManagementService.getConfiguration();
        final RuleSet addedRuleSet = configuration.getRoutingRules().getAccepted().get(REPOSITORY_GROUP_1);

        assertTrue(removed);
        assertNull(addedRuleSet);
    }

    @Test
    public void testAddAcceptedRepo()
            throws Exception
    {
        configurationManagementService.saveAcceptedRuleSet(getRuleSet());

        final boolean added = configurationManagementService.saveAcceptedRepository(REPOSITORY_GROUP_1,
                                                                                    getRoutingRule());
        final Configuration configuration = configurationManagementService.getConfiguration();

        assertTrue(added);

        configuration.getRoutingRules()
                     .getAccepted()
                     .get(REPOSITORY_GROUP_1)
                     .getRoutingRules()
                     .stream()
                     .filter(routingRule -> routingRule.getPattern().equals(RULE_PATTERN))
                     .forEach(routingRule -> assertTrue(routingRule.getRepositories().contains(REPOSITORY_RELEASES_2)));
    }

    @Test
    public void testRemoveAcceptedRepository()
            throws Exception
    {
        configurationManagementService.saveAcceptedRuleSet(getRuleSet());

        final boolean removed = configurationManagementService.removeAcceptedRepository(REPOSITORY_GROUP_1,
                                                                                        RULE_PATTERN,
                                                                                        REPOSITORY_RELEASES_1);

        final Configuration configuration = configurationManagementService.getConfiguration();
        configuration.getRoutingRules().getAccepted().get(REPOSITORY_GROUP_1).getRoutingRules().forEach(
                routingRule ->
                {
                    if (routingRule.getPattern().equals(RULE_PATTERN))
                    {
                        assertFalse(routingRule.getRepositories().contains(REPOSITORY_RELEASES_1));
                    }
                }
        );

        assertTrue(removed);
    }

    @Test
    public void testOverrideAcceptedRepositories()
            throws Exception
    {
        configurationManagementService.saveAcceptedRuleSet(getRuleSet());

        final RoutingRule rl = getRoutingRule();
        final boolean overridden = configurationManagementService.overrideAcceptedRepositories(REPOSITORY_GROUP_1, rl);
        final Configuration configuration = configurationManagementService.getConfiguration();
        configuration.getRoutingRules().getAccepted().get(REPOSITORY_GROUP_1).getRoutingRules().forEach(
                routingRule ->
                {
                    if (routingRule.getPattern().equals(rl.getPattern()))
                    {
                        assertEquals(1, routingRule.getRepositories().size());
                        assertEquals(rl.getRepositories(), routingRule.getRepositories());
                    }
                }
        );

        assertTrue(overridden);
    }

    @Test
    public void testCanGetRepositoriesWithStorageAndLayout()
    {
        String maven2Layout = Maven2LayoutProvider.ALIAS;
        List<Repository> repositories = configurationManagementService.getRepositoriesWithLayout(STORAGE0,
                                                                                                 maven2Layout);

        assertFalse(repositories.isEmpty());

        repositories.forEach(
                repository -> assertTrue(repository.getLayout().equals(maven2Layout))
        );

        repositories.forEach(
                repository -> assertTrue(repository.getStorage().getId().equals(STORAGE0))
        );
    }

    @Test
    public void testCanGetRepositoriesWithStorageAndLayoutNotExistedStorage()
    {
        String maven2Layout = Maven2LayoutProvider.ALIAS;
        List<Repository> repositories = configurationManagementService.getRepositoriesWithLayout("notExistedStorage",
                                                                                                 maven2Layout);

        assertTrue(repositories.isEmpty());
    }

    @Test(expected = OConcurrentModificationException.class)
    public void shouldProtectConcurrentModification()
            throws Exception
    {
        Configuration configuration = configurationManagementService.getConfiguration();

        Repository repository1 = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_4_DB_VERSION_1);
        createRepository(repository1);

        Repository repository2 = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_4_DB_VERSION_2);
        configuration.getStorage(STORAGE0).addRepository(repository2);

        configurationManagementService.save(configuration);
    }

    @Test(expected = OConcurrentModificationException.class)
    public void shouldProtectConcurrentModificationDuringSet()
            throws Exception
    {
        Configuration configuration = configurationManagementService.getConfiguration();

        Repository repository1 = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_4_DB_VERSION_1);
        createRepository(repository1);

        Repository repository2 = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_4_DB_VERSION_2);
        configuration.getStorage(STORAGE0).addRepository(repository2);

        configurationManagementService.setConfiguration(configuration);
    }

    private RoutingRule getRoutingRule()
    {
        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(RULE_PATTERN);
        routingRule.setRepositories(new HashSet<>(Collections.singletonList(REPOSITORY_RELEASES_2)));

        return routingRule;
    }

    private RuleSet getRuleSet()
    {
        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(RULE_PATTERN);
        routingRule.setRepositories(new HashSet<>(Collections.singletonList(REPOSITORY_RELEASES_1)));

        RuleSet ruleSet = new RuleSet();
        ruleSet.setGroupRepository(REPOSITORY_GROUP_1);
        ruleSet.setRoutingRules(Collections.singletonList(routingRule));

        return ruleSet;
    }

}
